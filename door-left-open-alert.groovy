/**
 *  Door Left Open Alert
 *
 *  Copyright 2015 Jake Burgy
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
 *  Based on "Left It Open" by the SmartThings team.
 */
definition(
    name: "Door Left Open Alert",
    namespace: "qJake",
    author: "Jake Burgy",
    description: "Alerts you if you've left a door open for longer than a specified number of minutes (with a cusotmized message).",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Categories/doorsAndLocks.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Categories/doorsAndLocks@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Categories/doorsAndLocks@2x.png")

preferences {

	section("Monitor this door or window") {
		input "contact", "capability.contactSensor"
	}
	section("And notify me if it's open for more than this many minutes") {
		input "openThreshold", "number", description: "Number of minutes"
	}
    section("With this message") {
    	input "message", "text", description: "Alert message"
    }
	section("Via text message at this number (or via push notification if not specified") {
		input "phone", "phone", title: "Phone number (optional)", required: false
	}
}

def installed() {
	log.trace "installed()"
	subscribe()
}

def updated() {
	log.trace "updated()"
	unsubscribe()
	subscribe()
}

def subscribe() {
	subscribe(contact, "contact.open", doorOpen)
	subscribe(contact, "contact.closed", doorClosed)
}

def doorOpen(evt)
{
	log.trace "doorOpen($evt.name: $evt.value)"
	def t0 = now()
	def delay = openThreshold * 60
	runIn(delay, doorOpenTooLong, [overwrite: false])
	log.debug "scheduled doorOpenTooLong in ${now() - t0} msec"
}

def doorClosed(evt)
{
	log.trace "doorClosed($evt.name: $evt.value)"
}

def doorOpenTooLong() {
	def contactState = contact.currentState("contact")
	if (contactState.value == "open") {
		def elapsed = now() - contactState.rawDateCreated.time
		def threshold = (openThreshold * 60000) - 1000
		if (elapsed >= threshold) {
			log.debug "Contact has stayed open long enough since last check ($elapsed ms):  calling sendMessage()"
			sendMessage()
		} else {
			log.debug "Contact has not stayed open long enough since last check ($elapsed ms):  doing nothing"
		}
	} else {
		log.warn "doorOpenTooLong() called but contact is closed:  doing nothing"
	}
}

void sendMessage()
{
	log.info msg
	if (phone) {
		sendSms phone, message
	}
	else {
		sendPush message
	}
}