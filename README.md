# sparta-spring-webservice-utils
Spring Web Service Utility library. Includes functionalities to enhance the use of spring-ws framework. 

Features:

* PayloadTransformedLoggingInterceptor - This is an interceptor that combines Request/Response transformation with logging. Useful when you need to mask the request in the log for sensitive information.
	
	
__How To use:__

Add in your applicationContext.xml
	
```
		<bean class="org.sparta.springwsutils.PayloadTransformedLoggingInterceptor">
			<property name="xslt" value="classpath:xslt/<YOUR XSLT>.xslt"/>
		</bean>
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
* PayloadLoggingClientInterceptor - Used to Log request/response for the client calls.  

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
	            </bean>
	        </list>
		</property>
		...
	</bean>

```
