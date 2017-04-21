package com.couchmate.teamcity.phabricator.tasks;

import com.couchmate.teamcity.phabricator.AppConfig;
import com.couchmate.teamcity.phabricator.CommandBuilder;
import com.couchmate.teamcity.phabricator.PhabLogger;
import com.couchmate.teamcity.phabricator.arcanist.ArcanistClient;
import com.couchmate.teamcity.phabricator.conduit.ConduitClient;
import jetbrains.buildServer.agent.BuildRunnerContext;

/**
 * Created by mjo20 on 10/15/2015.
 */
public class ApplyPatch extends Task {

    private PhabLogger logger;
    private AppConfig appConfig;
    private ArcanistClient arcanistClient = null;
    private ConduitClient conduitClient = null;
    private BuildRunnerContext runner;

    public ApplyPatch(BuildRunnerContext runner, AppConfig appConfig, PhabLogger logger){
        this.appConfig = appConfig;
        this.logger = logger;
        this.runner = runner;
    }

    @Override
    protected void setup() {
        logger.info(String.format("Phabricator Plugin: Applying Differential Patch %s", appConfig.getDiffId()));
        this.arcanistClient = new ArcanistClient(
                this.appConfig.getConduitToken(), this.appConfig.getWorkingDir(), this.appConfig.getArcPath());
        this.conduitClient = new ConduitClient(this.appConfig.getPhabricatorUrl().toString(), this.appConfig.getConduitToken());
    }

    @Override
    protected void execute() {
        try {
            CommandBuilder.Command patch = arcanistClient.patch(this.appConfig.getDiffId());
            int patchCode = patch.exec().join();
            logger.info(String.format("Patch exited with code: %d", patchCode));

            if(patchCode > 0){
                this.runner.getBuild().stopBuild("Patch failed to apply. Check the agent output log for patch failure detals.");
            }

        } catch (NullPointerException e) { logger.warn("AppPatchError", e); }
    }

    @Override
    protected void teardown() {

    }
}
