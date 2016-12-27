/**
 *  Security Camera Device
 *
 *  Copyright 2016 Sebastian Krupinski
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
metadata {
    definition (name: "Security Camera Device", namespace: "ksainc", author: "Sebastian Krupinski") {
        capability "Configuration"
        capability "Video Camera"
        capability "Video Capture"
        capability "Refresh"
        capability "Switch"

        // custom commands
        command "start"
    }

    preferences {
        input("Address", "string", title:"Address", description: "Please enter your camera's IP Address", required: true, displayDuringSetup: true)
        input("Port", "string", title:"Port", description: "Please enter your camera's Port", required: true, displayDuringSetup: true)
        input("Authentication", "bool", title:"Does Camera require User Auth?", description: "Please choose if the camera requires authentication (only basic is supported)", defaultValue: true, displayDuringSetup: true)
        input("Username", "string", title:"Username", description: "Please enter your camera's username", required: false, displayDuringSetup: true)
        input("Password", "string", title:"Password", description: "Please enter your camera's password", required: false, displayDuringSetup: true)
    }

    simulator {
        // TODO: define status and reply messages here
    }

    tiles(scale: 2) {
        multiAttributeTile(name: "videoPlayer", type: "videoPlayer", width: 6, height: 4) {
            tileAttribute("device.switch", key: "CAMERA_STATUS") {
                attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", action: "switch.off", backgroundColor: "#79b821", defaultState: true)
                attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", action: "switch.on", backgroundColor: "#ffffff")
                attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
                attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", action: "refresh.refresh", backgroundColor: "#F22000")
            }

            tileAttribute("device.errorMessage", key: "CAMERA_ERROR_MESSAGE") {
                attributeState("errorMessage", label: "", value: "", defaultState: true)
            }

            tileAttribute("device.camera", key: "PRIMARY_CONTROL") {
                attributeState("on", label: "Active", icon: "st.camera.dlink-indoor", backgroundColor: "#79b821", defaultState: true)
                attributeState("off", label: "Inactive", icon: "st.camera.dlink-indoor", backgroundColor: "#ffffff")
                attributeState("restarting", label: "Connecting", icon: "st.camera.dlink-indoor", backgroundColor: "#53a7c0")
                attributeState("unavailable", label: "Unavailable", icon: "st.camera.dlink-indoor", backgroundColor: "#F22000")
            }

            tileAttribute("device.startLive", key: "START_LIVE") {
                attributeState("live", action: "start", defaultState: true)
            }

            tileAttribute("device.stream", key: "STREAM_URL") {
                attributeState("activeURL", defaultState: true)
            }

        }


        main("videoPlayer")
        details(["videoPlayer"])
    }
}

mappings {
    path("/getInHomeURL") {
        action:
        [GET: "getInHomeURL"]
    }
}


def installed() {
    log.trace "Installed..."
    log.debug "Device Data... $device.ip"
    configure()
}

def updated() {
    log.trace "Updated..."
    configure()
}
// parse events into attributes
def parse(String description) {
    log.trace "Parsing..."
}

// handle commands
def configure() {
    log.trace "Configure..."

    sendEvent(name:"switch", value:"on")

    log.debug "State - $state"
    log.debug "$parent.state"

    state.Address = parent.state.Address
    state.Port = parent.state.Port
    state.Username = parent.state.Username
    state.Password = parent.state.Password
}

def start() {
    log.trace "Stream..."
    log.debug settings

    def data = [
            OutHomeURL  : "rtsp://${state.Username}:${state.Password}@${state.Address}:${state.Port}/Streaming/Channel/1",
            InHomeURL  : "rtsp://${state.Username}:${state.Password}@${state.Address}:${state.Port}/Streaming/Channel/1",
            ThumbnailURL: "http://cdn.device-icons.smartthings.com/camera/dlink-indoor@2x.png",
            cookie      : [key: "key", value: "value"]
    ]

    log.debug "$data"

    def event = [
            name           : "stream",
            value          : groovy.json.JsonOutput.toJson(data).toString(),
            data		   : groovy.json.JsonOutput.toJson(data),
            descriptionText: "Starting the livestream",
            eventType      : "VIDEO",
            displayed      : false,
            isStateChange  : true
    ]
    sendEvent(event)
}

def getInHomeURL() {
    [InHomeURL  : "rtsp://${state.Username}:${state.Password}@${state.Address}:${state.Port}/Streaming/Channel/1"]
}