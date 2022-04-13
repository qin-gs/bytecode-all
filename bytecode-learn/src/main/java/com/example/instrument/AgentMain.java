package com.example.instrument;

import java.lang.instrument.Instrumentation;

public class AgentMain {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        instrumentation.addTransformer(new MyClassFileTransformer(), true);
    }
}
