# sparta-spring-webservice-utils
Spring Web Service Utility library. Includes functionalities to enhance the use of spring-ws framework. 

##Features:##

### PayloadTransformedLoggingInterceptor ###

 This is an interceptor that combines Request/Response transformation with logging. Useful when you need to mask the request in the log for sensitive information.
	
	
__How To use:__

Add in your applicationContext.xml
	
```
<?xml version="1.0" encoding="UTF-8"?>

<beans xmlns="http://www.springframework.org/schema/beans"
	   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	   xmlns:sws="http://www.springframework.org/schema/web-services" 
	   xsi:schemaLocation="
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.1.xsd 
		http://www.springframework.org/schema/web-services http://www.springframework.org/schema/web-services/web-services-2.0.xsd">

	<sws:interceptors>
		<bean class="org.sparta.springwsutils.PayloadTransformedLoggingInterceptor">
			<property name="xslt" value="classpath:xslt/<YOUR XSLT>.xslt"/>
		</bean>
	</sws:interceptors>
	....
</beans>
```

If you are using java-based configuration you should add to you WebServiceConfiguration class.

```
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {


    @Value("classpath:xslt/maskInput.xslt")
    private Resource xslt;
    
    @Autowired
    PayloadTransformedLoggingInterceptor logInterceptor;
    
    @Bean
    public PayloadTransformedLoggingInterceptor logInterceptor() {
        PayloadTransformedLoggingInterceptor logInterceptor = new PayloadTransformedLoggingInterceptor();
        logInterceptor.setXslt(xslt);
        return logInterceptor;
    }
    
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(logInterceptor);
    }
    ...
}
```




And then configure what do you want to mask in you xslt. For instance if you want do mask credit card:

```
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:typ="http://your/types"
    version="1.0">


    <!-- copy all document -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <!-- mask accountNumber -->
	<xsl:template match="typ:creditCardNumber">
		<xsl:copy>
			<xsl:value-of select="substring('*****************************************', 1, string-length(.)-4)"/>
			<xsl:value-of select="substring(.,string-length(.)-3,string-length(.)+1)" />
		</xsl:copy>
	</xsl:template>
</xsl>
```
<br/>

###PayloadLoggingClientInterceptor###

Used to Log request/response for the client calls.  

Has a more fine granularity than WebServiceTemplate (org.springframework.ws.client.MessageTracing).

Enables control for different clients defining different packages.

Adds the hability to log response timings.


__How To use:__

Add it as an interceptor for you WebServiceTemplate
	
```
	<!-- Spring WS configuration -->
	<bean id="webServiceTemplate" class="org.springframework.ws.client.core.WebServiceTemplate">
	 ...
		<property name="interceptors">
	       <list>
	            <bean class="org.sparta.springwsutils.PayloadLoggingClientInterceptor">
	            	<property name="requestLoggerName" value="com.sample.request"/>
	            	<property name="responseLoggerName" value="com.sample.response"/>
	            	<property name="enableLogTiming" value="true"/>
	               	<property name="removeNewline" value="true"/>
	            </bean>
	        </list>
		</property>
		...
	</bean>

```

options: 

* __requestLoggerName__: name of the category that will be logged in SL4J for the request. If not provided will use the default value: org.sparta.springwsutils.PayloadLoggingClientInterceptor.request
* __responseLoggerName__: name of the category that will be logged in SL4J for the response. If not provided will use the default value: org.sparta.springwsutils.PayloadLoggingClientInterceptor.response
* __enableLogTiming__: If set to true it will add the total timing of the execution in the response line. Default is false.
* __removeNewLine__: If set to true it will remove all break lines from the request/response payload logging. 


### MDCInfoInjectInterceptor ###

 This is an interceptor that adds a MDC value UUID, that can be used in log for trace purposes. 
	
	
__How To use:__

Add in your applicationContext.xml
	
```
	<!-- Spring WS configuration -->
	<bean id="webServiceTemplate" class="org.springframework.ws.client.core.WebServiceTemplate">
	 ...
		<property name="interceptors">
	       <list>
	            <bean class="org.sparta.springwsutils.MDCInfoInjectInterceptor"/>
	        </list>
		</property>
		...
	</bean>			
```

If you are using java-based configuration you should add to you WebServiceConfiguration class.

```
@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Autowired
    MDCInfoInjectInterceptor mdcInterceptor;
    
    @Bean
    public MDCInfoInjectInterceptor mdcInterceptor() {
        return new MDCInfoInjectInterceptor();
    }
    
    @Override
    public void addInterceptors(List<EndpointInterceptor> interceptors) {
        interceptors.add(mdcInterceptor);
    }
    ...
}
```




And then configure your log tool to show this MDC value. example for logback.xml.

```
<!DOCTYPE xml>
<configuration scan="true" scanPeriod="60 seconds"> 

	<property name="FILE_LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%X{UUID}] [%thread] %-5level %logger{36} - %msg%n"/>

	<appender name="FILE"
		class="ch.qos.logback.core.rolling.RollingFileAppender">
		<encoder>
			<pattern>${FILE_LOG_PATTERN}</pattern>
		</encoder>
		<file>app.log</file>
		<rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
			<!-- daily rollover -->
			<fileNamePattern>app.%d{yyyy-MM-dd}.log.gz</fileNamePattern>

			<!-- keep 365 days' worth of history -->
			<maxHistory>365</maxHistory>
		</rollingPolicy>
	</appender>
	    
</configuration>
```
<br/>


