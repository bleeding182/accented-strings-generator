# Accented Strings Generator
Gradle plugin for android to generate accented english resources (values-zz) for your current Strings.

It will ignore `donottranslate.xml` and `translatable=false` strings

## Features
The plugin will generate an accented variant of all your strings from `res/values` in `build/generated/res/accented/buildType/values-zz`.

Iff you use this plugin, be sure not to set any strings in `values-zz` yourself or a conflict merging resources will occur.

## Usage
Add the buildscript and apply the plugin to your android project.

    buildscript {
        repositories {
            maven {
                // currently staging repository
            }
        }
        dependencies {
            classpath 'com.github.bleeding182.gradle:accented-strings-generator:1.0.0-rc1'
        }
    }
    
    apply plugin: 'com.android.application'
    
    // apply the plugin to generate values-zz
    apply plugin: 'accented-strings'

## Example
###### Input `src/main/res/values/strings.xml`

    <?xml version="1.0" encoding="utf-8" standalone="no"?>
    <resources>
        <string name="action_login">Log in</string>
        <string name="action_switch">Switch</string>
        <string name="action_settings">Settings</string>
        <string name="action_create">Create</string>
        <string name="action_start">Start</string>
        <string name="action_new_user">New user</string>
    </resources>
    
###### Output `build/generated/res/accented/main/res/values-zz/strings.xml`
    
    <?xml version="1.0" encoding="utf-8" standalone="no"?>
    <resources>
        <string name="action_login">Łög îñ</string>
        <string name="action_switch">Śwîtčh</string>
        <string name="action_settings">Śëttîñgš</string>
        <string name="action_create">Črëātë</string>
        <string name="action_start">Śtārt</string>
        <string name="action_new_user">Ńëw üšër</string>
    </resources>
