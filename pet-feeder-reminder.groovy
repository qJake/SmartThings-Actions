/**
 *  Pet Feeder Reminder
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
 */
/**
 * Based on "Has Barkley Been Fed?" by the SmartThings team.
 * https://github.com/rappleg/SmartThings/blob/master/smartapps/smartthings/has-barkley-been-fed.groovy
 */
 
definition(
    name: "Pet Feeder Reminder",
    namespace: "qJake",
    author: "Jake Burgy",
    description: "Reminds you to feed your pets between two times in a certain day if a motion sensor has not moved within that timeframe.",
    category: "Pets",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
	section("1. Choose your pet feeder sensor.") {
		input "feeder", "capability.contactSensor", title: "Sensor:", required: true
	}
	section("2. Specify when you feed your pets.") {
		input "timefrom", "time", title: "Between:", required: true
		input "timeto", "time", title: "And:", required: true
	}
	section("3. If I forget by the ending time, text me.") {
		input "phone1", "phone", title: "Phone number:", required: true
		input "phone2", "phone", title: "Secondary number: (Optional)", required: false
	}
}

def installed()
{
	schedule(timeto, "scheduleCheck")
}

def updated()
{
	unsubscribe() //TODO no longer subscribe like we used to - clean this up after all apps updated
	unschedule()
	schedule(timeto, "scheduleCheck")
}

def scheduleCheck()
{
	log.trace "scheduledCheck"

	def from = new Date(timefrom)
	def now = new Date()
	def feederEvents = feeder.eventsBetween(from, now)
	log.trace "Found ${feederEvents?.size() ?: 0} feeder events since $timefrom"
	def feederOpened = feederEvents.count { it.value && it.value == "open" } > 0

	if (feederOpened) {
		log.debug "Feeder was opened since $timefrom, no SMS required"
	} else {
		log.debug "Feeder was not opened since $timefrom, texting $phone1 (and optionally, $phone2)"
		sendSms(phone1, "Oops! Don't forget to feed the pets!")
		sendSms(phone2, "Oops! Don't forget to feed the pets!")
	}
}