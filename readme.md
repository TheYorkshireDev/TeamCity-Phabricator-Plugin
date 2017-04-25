Automattic Development Notes:

The current production version for the TeamCity Plugin can be found in tearmicty-release/v1.1.  The php files for the phabricator side of the plugin can be found in phabricator-release/v1.1:

Description of directories:

The primary directories for Automattic development are `teamcity-wrking`, `teamcity-test`, `phabricator-release`, and `teamcity-release`.  Because we have updated this project just by modifying and recompling the java vs. using maven our development process is a bit different.

`teamcity-wrking` should be used to make changes to the java files and recompile.  This directory should also be used to update jar files with the newly compiled updates.

`teamcity-test` contains only the files necessary for zipping the plugin in the format to be uploaded to TC.  Any updated jar files from `teamcity-wrking` should also be copied here.

`agent` and `server` contain all custom (non-jetpack developed) `.java` files.  If modifying a java file that does not exist in `teamcity-wrking`, copy the original java file from the relvevant directory and add it to teamcity-wrking.

Development Workflow:

At this point, only files from `agent-1.0-SNAPSHOT.jar` have needed modification.  To apply changes, `teamcity-wrking` was created by unzipping `TeamCity-Phabricator-Plugin/release/v1.0/teamcity-phabricator-plugin.zip`, and by unpacking `agent-1.0-SNAPSHOT.jar`.  The java files present in that directory were added manually by coping from the `agent` directory.
 
`teamcity-wrking` also contains additional jar files, that were not included when `teamcity-phabricator-plugin.zip` was unzipped.  Through updating the java files, it became evident we were missing some dependencies, those jar files were added here to compile updated java files.

Missing Jetbrains JAR packages can be found and downloaded here: https://download.jetbrains.com/teamcity-repository/org/jetbrains/teamcity/

After the relevant jar file has been updated it should be copied to replace the existing copy in `teamcity-test`.  `teamcity-test` is where the plugin should be zipped into it's final format for uploading to TC.

Note on zipping: For TC to read the plugin it must be zipped in a very specific format. `teamcity-test/agent/phabricator-agent/` needs to be zipped separately first. When unzipped`phabricator-agent.zip` should follow this structure, with a top-level directory named `phabricator-agent`:

phabricator-agent
  |
  | -- lib
  	|
  	| -- agent-1.0-SNAPSHOT.jar
  	|
  	| -- common-1.0-SNAPSHOT.jar, 
  	|
  	| -- remaing jar files, etc.
  |
  | -- teamcity-plugin.xml


Once `phabricator-agent.zip` is zipped inside `teamcity-test/agent` the unzipped copy of the files should be removed.  After removing unzipped files from `teamcity-test/agent` the entire `teamcity-test` directory can be zipped to create the final upload file `teamcity-phabricator-plugin.zip`  The `teamcity-test/` directory has been left as a sample, and the removal of files should not be commited the repo.



Helpful Commands:

To unpack a jar file:
jar xf agent-1.0-SNAPSHOT.jar

To compile changed java file – this will generate a new class file: 
javac -extdirs "./" com/couchmate/teamcity/phabricator/Agent.java

To update a jar file:
jar vuf agent-1.0-SNAPSHOT.jar com/couchmate/teamcity/phabricator/tasks/ApplyPatch.class

Zipping the phabricator-agent file:
agent xxx$ zip phabricator-agent.zip ./* -r

Zipping the entire plugin:
teamcity-phabricator-plugin xxx$ zip teamcity-phabricator-plugin.zip ./* -r



------------------------------------------------------


***Note***: I'm not actively maintaining this repo at the moment as I'm not using Phabricator or TC. Feel free to submit PRs or fork for your own use!.

#TeamCity Phabricator Plugin
Real-time build triggers and reporting with JetBrain's TeamCity and Phacility's Phabricator (Harbormaster)

We currently host this repo in Phabricator (and this repo is a mirror). If you submit PRs, we'll pull them in locally, test (until Travis/Circle gets setup) and commit them on our end, which will then be incorporated here. We may migrate all of this exclusively to GH but for now, this is the plan.

##Installation
The plugin consists of two pieces: the TeamCity Java plugin (Build Feature) which itself consists of two parts: a Server and Agent plugin, as well as the custom Phabricator Harbormaster Build Step.

High Level Steps:

1. Upload `teamcity-phabricator-plugin.zip` as an external plugin
2. Move the PHP files from the Phabricator/ directory of the release into `src/extensions` on your Phabricator instance
3. ???
4. Profit

###TeamCity
Installing the TeamCity plugin is extremely easy. In the latest release, unzip the release and in the TeamCity folder is `teamcity-phabricator-plugin.zip`. This zip is uploaded directly to your TeamCity instance. The plugin is automatically applied to all of your available Agents but requires a reboot to apply the Server side of the plugin.

After you install and bounce the Server, you'll now be able to apply a custom Build Feature, "Phabricator Plugin". After selecting the plugin, you should be presented with the following prompt:

![Phabricator Plugin Prompt]
(https://i.imgur.com/KArV0GP.png)

***Phabricator URL***: The domain of your Phabricator installation, e.g. phabricator.example.com

***Conduit Token***: The Conduit API Key for the user you want to be executing build actions. This probably should be a Phabricator Bot as a best practice.

***Path To Anarcist***: This is the path to Anarcist on your build agents. Currently, the plugin relies on a hardcoded path to Anarcist. In the future, this will simply look for `arc`.

After you successfully fill out the preceding options, you need to make a change to your VCS Checkout Settings for the project you plan on using the plugin with.

Change the `Checkout Options` to `Automatically On Agent`. This enables us to execute actions against
our checked out code (clean, reset) and apply Differential patches without lock errors.

![Checkout Change]
(https://i.imgur.com/IFBbx0s.png)

###Phabricator
Installing the Phabricator piece is a little bit more involved but still pretty easy. In the
Phabricator folder from the release zip, there are two files (1) `HarbormasterTeamCityBuildStepImplementation.php`
and (2)`TeamCityXmlBuildBuilder.xml`. Move these files into your Phabricator's `src/extensions` directory.
They will automatically be consumed by Phabricator and be ready to use.

Now you're ready to add TeamCity into your Harbormaster Build workflow. The most common use case is to
create a new Herold rule that, on every diff pushed, runs any number of given build plans. When adding
the new TeamCity Build Plan, you'll be presented with the following options:

![Build Step]
(https://i.imgur.com/8aUgk5q.png)

***URI***: The URL to your TeamCity instance.
***TeamCity Build Configuration ID***: This is the ID given to your project when it is initially created.
![BuildId]
(https://i.imgur.com/P9hOc5s.png)
***TeamCity Credentials***: You must add a set of credentials (username/password) that has access to your
TeamCity installation in order to make RESTful calls.

After all of that is complete, add the Build Step to a Herold rule for any of your projects, push a diff to
said project and watch the magic happen!

##Functionality
The TeamCity plugin currently reports the following to Phabricator (Harbormaster):

1. Build Pass/Fail on completion
2. Any Unit Tests you have defined in your build steps and their pass/fail status

##Future Features

1. Add more detailed Unit Test reporting, like duration
2. Add lint test reporting
3. Add Differential commenting

##Contributions
I want to give a special shoutout to [@sectioneight](https://github.com/sectioneight) for the inspiration on how to scaffold out this plugin
(He wrote a majority of [Uber's Jenkins->Phabricator plugin](https://github.com/uber/phabricator-jenkins-plugin) and [@joprice](https://github.com/joprice) for moral support
