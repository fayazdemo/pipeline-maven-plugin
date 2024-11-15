package org.jenkinsci.plugins.pipeline.maven.listeners;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import hudson.model.Result;
import java.util.Arrays;
import java.util.Collections;
import org.jenkinsci.plugins.pipeline.maven.GlobalPipelineMavenConfig;
import org.jenkinsci.plugins.pipeline.maven.WithMavenStep;
import org.jenkinsci.plugins.pipeline.maven.dao.PipelineMavenPluginDao;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatabaseSyncRunListenerTest {

    @InjectMocks
    private DatabaseSyncRunListener listener;

    @Mock
    private GlobalPipelineMavenConfig config;

    @Mock
    private PipelineMavenPluginDao dao;

    @Mock
    private WorkflowRun build;

    @Mock
    private WorkflowJob job;

    @Mock
    private FlowExecutionOwner flowExecutionOwner;

    @Mock
    private FlowExecution flowExecution;

    @Mock
    private FlowNode flowNode;

    @Test
    public void test_abort_when_step_not_run() throws Exception {
        when(build.asFlowExecutionOwner()).thenReturn(flowExecutionOwner);
        when(flowExecutionOwner.get()).thenReturn(flowExecution);
        when(flowExecution.getCurrentHeads()).thenReturn(Collections.emptyList());

        listener.onCompleted(build, null);

        verifyNoInteractions(dao);
    }

    @Test
    public void test_on_completed() throws Exception {
        when(build.asFlowExecutionOwner()).thenReturn(flowExecutionOwner);
        when(flowExecutionOwner.get()).thenReturn(flowExecution);
        when(flowExecution.getCurrentHeads()).thenReturn(Arrays.asList(flowNode));
        when(flowNode.getDisplayFunctionName()).thenReturn(WithMavenStep.DescriptorImpl.FUNCTION_NAME);
        when(build.getParent()).thenReturn(job);
        when(job.getFullName()).thenReturn("the job");
        when(build.getNumber()).thenReturn(42);
        long startTime = System.currentTimeMillis() + 1000;
        when(build.getStartTimeInMillis()).thenReturn(startTime);
        when(config.getDao()).thenReturn(dao);

        listener.onCompleted(build, null);

        verify(dao).updateBuildOnCompletion("the job", 42, Result.SUCCESS.ordinal, startTime, 0);
    }

    @Test
    public void test_on_completed_with_build_result() throws Exception {
        when(build.asFlowExecutionOwner()).thenReturn(flowExecutionOwner);
        when(flowExecutionOwner.get()).thenReturn(flowExecution);
        when(flowExecution.getCurrentHeads()).thenReturn(Arrays.asList(flowNode));
        when(flowNode.getDisplayFunctionName()).thenReturn(WithMavenStep.DescriptorImpl.FUNCTION_NAME);
        when(build.getParent()).thenReturn(job);
        when(job.getFullName()).thenReturn("the job");
        when(build.getNumber()).thenReturn(42);
        long startTime = System.currentTimeMillis() + 1000;
        when(build.getStartTimeInMillis()).thenReturn(startTime);
        when(build.getResult()).thenReturn(Result.UNSTABLE);
        when(config.getDao()).thenReturn(dao);

        listener.onCompleted(build, null);

        verify(dao).updateBuildOnCompletion("the job", 42, Result.UNSTABLE.ordinal, startTime, 0);
    }

    @Test
    public void test_on_deleted() {
        when(build.getParent()).thenReturn(job);
        when(job.getFullName()).thenReturn("the job");
        when(build.getNumber()).thenReturn(42);
        when(config.getDao()).thenReturn(dao);

        listener.onDeleted(build);

        verify(dao).deleteBuild("the job", 42);
    }
}
