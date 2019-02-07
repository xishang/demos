package com.demo.framework.hystrix;

import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author xishang
 * @version 1.0
 * @since 2018/6/19
 * <p>
 * 请求合并
 */
public class CommandCollapser extends HystrixCollapser<List<String>, String, Integer> {

    private final Integer key;

    public CommandCollapser(Integer key) {
        this.key = key;
    }

    @Override
    public Integer getRequestArgument() {
        return key;
    }

    @Override
    protected HystrixCommand<List<String>> createCommand(final Collection<CollapsedRequest<String, Integer>> requests) {
        return new BatchCommand(requests);
    }

    @Override
    protected void mapResponseToRequests(List<String> batchResponse, Collection<CollapsedRequest<String, Integer>> requests) {
        int count = 0;
        for (CollapsedRequest<String, Integer> request : requests) {
            request.setResponse(batchResponse.get(count++));
        }
    }

    private static final class BatchCommand extends HystrixCommand<List<String>> {
        private final Collection<CollapsedRequest<String, Integer>> requests;

        private BatchCommand(Collection<CollapsedRequest<String, Integer>> requests) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ExampleGroup"))
                    .andCommandKey(HystrixCommandKey.Factory.asKey("GetValueForKey")));
            this.requests = requests;
        }

        @Override
        protected List<String> run() {
            ArrayList<String> response = new ArrayList<String>();
            for (CollapsedRequest<String, Integer> request : requests) {
                // artificial response for each argument received in the batch
                response.add("ValueForKey: " + request.getArgument());
            }
            return response;
        }
    }

    public static void assertEquals(Object arg1, Object arg2) {
        if (arg1 != arg2 && !arg1.equals(arg2))
            throw new RuntimeException(arg1 + " != " + arg2);
    }

    public static void main(String[] args) throws Exception {
        HystrixRequestContext context = HystrixRequestContext.initializeContext();
        try {
            Future<String> f1 = new CommandCollapser(1).queue();
            Future<String> f2 = new CommandCollapser(2).queue();
            Future<String> f3 = new CommandCollapser(3).queue();
            Future<String> f4 = new CommandCollapser(4).queue();

            assertEquals("ValueForKey: 1", f1.get());
            assertEquals("ValueForKey: 2", f2.get());
            assertEquals("ValueForKey: 3", f3.get());
            assertEquals("ValueForKey: 4", f4.get());

            // assert that the batch command 'GetValueForKey' was in fact
            // executed and that it executed only once
            assertEquals(1, HystrixRequestLog.getCurrentRequest().getExecutedCommands().size());
            HystrixCommand<?> command = HystrixRequestLog.getCurrentRequest().getExecutedCommands().toArray(new HystrixCommand<?>[1])[0];
            // assert the command is the one we're expecting
            assertEquals("GetValueForKey", command.getCommandKey().name());
            // confirm that it was a COLLAPSED command execution
            assertEquals(command.getExecutionEvents().contains(HystrixEventType.COLLAPSED), true);
            // and that it was successful
            assertEquals(command.getExecutionEvents().contains(HystrixEventType.SUCCESS), true);
        } finally {
            context.shutdown();
        }
    }

}