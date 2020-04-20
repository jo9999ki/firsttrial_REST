# Application firsttrial_REST

This is an Spring Boot 2 Project for Java 11 and Maven 3.3+ containing Micrometer metrics cabilities with Prometheus and Graphana.

It part of a spring boot tutorial containing follwing chapters
1. Create Spring boot 2 project with H2 database, Data Model, Hibernate Repository
2. Add internal CRUD service with basic test capability
3. Create REST Controller, add error output and error handler, create JUnit test for REST controller
4. Add swagger (OpenAPI) documentation for REST controller
5. Add Spring Boot Actuator with Micrometer Metrics, local Prometheus and Graphana severs and customized health check

## Activate Metrics endpoints (Prometheus and other)
* Enhance POM with Spring Boot Actuator, Metrics Base and Prometheus dependencies:
<pre><code>
        &lt;!-- Spring boot actuator to expose metrics endpoint --&gt;
        &lt;dependency&gt;
                &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
                &lt;artifactId&gt;spring-boot-starter-actuator&lt;/artifactId&gt;
        &lt;/dependency&gt;
        &lt;!-- Micormeter core dependecy  --&gt;
        &lt;dependency&gt;
                &lt;groupId&gt;io.micrometer&lt;/groupId&gt;
                &lt;artifactId&gt;micrometer-core&lt;/artifactId&gt;
        &lt;/dependency&gt;
        &lt;!-- Micrometer Prometheus registry  --&gt;
        &lt;dependency&gt;
                &lt;groupId&gt;io.micrometer&lt;/groupId&gt;
                &lt;artifactId&gt;micrometer-registry-prometheus&lt;/artifactId&gt;
        &lt;/dependency&gt;
</pre></code>		
		
* Configure endpoints to be enabled:
<pre><code>
        management.endpoint.metrics.enabled=true
        management.endpoints.web.exposure.include=* --> To be replaced by single endpoints names (comma separated) later
        management.endpoint.prometheus.enabled=true
        management.metrics.export.prometheus.enabled=true
        management.endpoint.httptrace.enabled=true
</pre></code>

* Enhance Config
<pre><code>
        //Metrics
        @Bean
        MeterRegistryCustomizer&lt;MeterRegistry&gt; metricsCommonTags() {
          return registry -&gt; registry.config().commonTags(&quot;app.name&quot;, &quot;firsttrial&quot;);
        }
        @Bean
        TimedAspect timedAspect(MeterRegistry registry) {
          return new TimedAspect(registry);
        }
        @Bean
        public HttpTraceRepository httpTraceRepository() {
                return new InMemoryHttpTraceRepository();
        }
</pre></code>

* Run application, use swagger ui to send requests, finally try following actuatorendpoints:<br>
http://localhost:8080/actuator -&gt; Overview all activated endpoints <br>
http://localhost:8080/actuator/prometheus -&gt; metrics with prometheus endpoint -&gt; see entries for customer requests <br>
http://localhost:8080/actuator/info -&gt; Shows current application infos - can be customized<br>
http://localhost:8080/actuator/health -&gt; Shows default health check - can be customized<br>
http://localhost:8080/actuator/metrics -&gt; Overview of metrics content, drilldown possible with filter parameters<br>
http://localhost:8080/actuator/metrics/http.server.requests?tag=uri:/customers -&gt; Example drilldown for REST endpoints<br>
http://localhost:8080/actuator/loggers -&gt; Overview of all loggers<br>
http://localhost:8080/actuator/httptrace -&gt; Detailed infos single http requests<br>

## Create addtional metrics entries "method_timed_ ..." for REST endpoints
 <br>Enhance methods in REST controller with Annotation @Timed
 <br>Check for new entries "method_timed_seconds_..." in endpoint prometheus
 <br>Check for new entries in endpoint metrics 
 <br> http://localhost:8080/actuator/metrics/method.timed?tag=method:getAllcustomersWithPagination
 
## Step 2: Create customized health check
* Code enhancements
<br> Create new package "... health"
<br> Create new class
<pre><code>
        @Component
        public class HealthCheck implements HealthIndicator {

                @Autowired
                CustomerRepository repository;
                @Override
                public Health health() {
                        int errorCode = check(); // perform some specific health check
                        if (errorCode != 0) {
                                return Health.down()
                                  .withDetail("No customer record found in database", errorCode).build();
                        }
                        return Health.up().build();
                }

                public int check() {
                        Pageable paging = PageRequest.of(0, 10,Sort.by("id"));
                        Page<CustomerEntity> pagedResult = repository.findAll(paging);

                        if(pagedResult.hasContent()) {
                                return 0;
                        } else {
                                return 1;
                        }
                }
        }
</pre></code>

<br> Check health endpoint: http://localhost:8080/actuator/health





			