/**
 *  My First SmartApp
 *
 *  Copyright 2016 Ian McMahon
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Motion Activated Switch",
    namespace: "ianmcmahon",
    author: "Ian McMahon",
    description: "Turns on a switch whenever motion is detected, turning it off after a delay when motion ceases.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Devices") {
    	input "themotion", "capability.motionSensor", required: true, title: "Motion Sensor"
    	input "theswitch", "capability.switchLevel", required: true, title: "Switch/Light"
	}
    section("Settings") {
    	input "minutes", "number", required: true, title: "Delay (minutes)"
        input "level", "number", required: true, title: "Dimmer Level (0-100)"
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
	subscribe(themotion, "motion.active", motionDetectedHandler)
	subscribe(themotion, "motion.inactive", motionStoppedHandler)
}

def motionDetectedHandler(evt) {
    theswitch.setLevel(level)
}

def motionStoppedHandler(evt) {
	runIn(60 * minutes, checkMotion)
}

def checkMotion() {
    log.debug "In checkMotion scheduled method"

	def motionState = themotion.currentState("motion")
    
    if (motionState.value == "inactive") {
    	def elapsed = now() - motionState.date.time
        
        def threshold = 1000 * 60 * minutes // convert from ms
        
        if (elapsed >= threshold) {
            log.debug "Motion has stayed inactive long enough since last check ($elapsed ms):  turning switch off"
            
        	theswitch.off()
        } else {
            log.debug "Motion has not stayed inactive long enough since last check ($elapsed ms):  doing nothing"
            runIn(threshold - elapsed / 1000, checkMotion)
        }
	} else {
        // Motion active; just log it and do nothing
        log.debug "Motion is active, do nothing and wait for inactive"
  	}
}
	