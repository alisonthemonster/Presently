#!/bin/bash

function copyEnvVarsToGradleProperties {
    LOCAL_PROPERTIES=$HOME"/.gradle/local.properties"
    export LOCAL_PROPERTIES
    echo "Local Properties should exist at $LOCAL_PROPERTIES"

    if [ ! -f "$LOCAL_PROPERTIES" ]; then
        echo "Local Properties does not exist"

        echo "Creating Local Properties file..."
        touch $LOCAL_PROPERTIES

        echo "Writing DROPBOX_KEY to local.properties..."
        echo "DROPBOX_KEY=$DROPBOX_APP_KEY" >> $LOCAL_PROPERTIES
    fi
}