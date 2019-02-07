# Java Instrumentation API
  
  ## 1.PreMain
  
  Agent和Target必须同时启动
  
      java -javaagent:agent.jar[=args] -jar target.jar
      
  eg: java -javaagent:jmx.jar -jar jmx.jar
      
  ## 2.AgentMain
  
  Agent可以随时attach到Target的JVM实例
  
  通过main()方法调用attach API, eg:
  
      java -Xbootclasspath/a:$JAVA_HOME/lib/tools.jar -jar jmx.jar  6898 "arg1=123;arg2=456"