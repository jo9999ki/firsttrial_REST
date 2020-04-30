# Application firsttrial

This is an Spring Boot 2 Project for Java 11 and Maven 3.3+ containing Micrometer metrics cabilities with Prometheus and Graphana.
The code contains best case approaches for fast project adaption.

Its part of a spring boot tutorial containing follwing chapters
1. Create Spring boot 2 project with H2 database, Data Model, Hibernate Repository
2. Add internal CRUD service with basic test capability
3. Create REST Controller, add error output and error handler, create JUnit test for REST controller
4. Add swagger (OpenAPI) documentation for REST controller
5. Analyzability Features (current repository)
<br> --> Add Spring Boot Actuator with Micrometer Metrics - enables monitoring capabilities with Prometheus and Graphana severs - and add customized health check
<br> --> Log requests and response with Logbook
<br> --> Tracing for internal service walkthroughs and distributed service calls with Spring Cloud Sleuth
6. Cloud-readiness - create and deploy docker image (current repository)
<br> --> add customized Dockerfile to project
<br> --> build image, test with local Docker desktop and push image to Dockerhub
<br> --> deploy image on local minikube (Kubernetes with one node)

The current repository contains full code with latest enhancements

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

### Create addtional metrics entries "method_timed_ ..." for REST endpoints
* Enhance methods in REST controller with Annotation @Timed
 <br>Check for new entries "method_timed_seconds_..." in endpoint prometheus
 <br>Check for new entries in endpoint metrics 
 <br> http://localhost:8080/actuator/metrics/method.timed?tag=method:getAllcustomersWithPagination
 
## Create customized health check
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

<br> Run application and check content changes in health endpoint: http://localhost:8080/actuator/health

## Monitor metrics with Prometheus and Graphana
* Install and use Prometheus server
<br> See instructions in https://dzone.com/articles/monitoring-using-spring-boot-20-prometheus-and-gra
<br> Download installation package from https://prometheus.io/download/ and extract in local directory
<br> Update config file prometheus.yml in following part:
<pre><code>
	scrape_configs:
	  # The job name is added as a label `job=<job_name>` to any timeseries scraped from this config.
	  - job_name: 'prometheus'

		# metrics_path defaults to '/metrics'
		metrics_path: /actuator/prometheus
		# scheme defaults to 'http'.

		static_configs:
		- targets: ['localhost:8080']
</pre></code>		
<br> start prometheus server (prometheus.exe)
<br> Open Web site in browser http://localhost:9090 and create graphs
			
* Install and user Graphana
<br> See instructions in https://dzone.com/articles/monitoring-using-spring-boot-20-prometheus-and-gra
<br> Download from https://grafana.com/grafana/download?platform=windows and extract in local directory
<br> Start server (grafana-server.exe)
<br> open web site in browser http://localhost:3000
<br> Change password
<br> Add prometheus as data source: http://localhost:9090
<br> Add panels for imported data

## HTTP Request and Response Logging with Zalando Logbook
* Zalando Logbook logs both entire request and response content. Without this library an customized interceptor must be created.
* Add dependency in POM file
<pre><code>
	&lt;dependency&gt;
	   &lt;groupId&gt;org.zalando&lt;/groupId&gt;
	   &lt;artifactId&gt;logbook-spring-boot-starter&lt;/artifactId&gt;
	   &lt;version&gt;1.5.0&lt;/version&gt;
	&lt;/dependency&gt;
</pre></code>
* Additionally add configuration to enable request and response logging and exclude not required endpoints
<pre><code>
	logging.level.org.springframework.web=ERROR
	#Set Logbook Level &gt; DEBUG (INFO, ERROR, ...) to enable logging
	logbook.write.level=INFO
	logbook.exclude=//actuator/info, //actuator/health, /webjars/**, /swagger-resources/**, /swagger-ui.html, /v2/api-docs, /error, /csrf
</pre></code>
<br> Remarks: Logbook works only with certain versions correctly. It has rare documentation, so it was not possible to set filters to get logs for error cases only (e.g. certain http codes). Logbook log level must be INFO or higher. It cannot be activated in error case only. My recommendation is to use INFO as most log entries are non error cases.

## HTTP Distributed tracing in single service with Spring Boot Sleuth
* Spring Sleuth introduces unique tracing ids for all logs included in code path of one request (e.g. Controller -> Service -> Repository -> ...) 
<br> Activate Sleuth with following enhancements in POM file
<pre><code>
&lt;properties&gt;
	(...)
	&lt;spring-cloud.version&gt;Hoxton.SR3&lt;/spring-cloud.version&gt;
&lt;/properties&gt;
</pre></code>
<pre><code>
&lt;dependencies&gt;
	(...)
	&lt;!--  Distributed tracing --&gt;
	&lt;dependency&gt;
		&lt;groupId&gt;org.springframework.cloud&lt;/groupId&gt;
		&lt;artifactId&gt;spring-cloud-starter-sleuth&lt;/artifactId&gt;
	&lt;/dependency&gt;		
&lt;/dependencies&gt;
</pre></code>
<pre><code>
&lt;dependencyManagement&gt;
	&lt;dependencies&gt;
		&lt;dependency&gt;
			&lt;groupId&gt;org.springframework.cloud&lt;/groupId&gt;
			&lt;artifactId&gt;spring-cloud-dependencies&lt;/artifactId&gt;
			&lt;version&gt;${spring-cloud.version}&lt;/version&gt;
			&lt;type&gt;pom&lt;/type&gt;
			&lt;scope&gt;import&lt;/scope&gt;
		&lt;/dependency&gt;
	&lt;/dependencies&gt;
&lt;/dependencyManagement&gt;
</pre></code>
* Sleuth requires an application name (here typically logical service name), which must be added to application.properties:
<br>--> spring.application.name=customer service

* To avoid conflicts, existing logging patterns migth be to be removed from application.properties. Otherwise logging might not be enhanced by Sleuth!!!
<br> -->remove or deactivate #logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n 

* Enhance Exception method for input validation in CustomExceptionHandler by error logging

* Finally start application and run POST method in Swagger UI with invalid mail address
<br> Example:
<pre><code>
	2020-04-22 20:22:08.722  INFO [customer service,eea0fa6b27939e0e,eea0fa6b27939e0e,false] 6256 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;remote&quot;,&quot;type&quot;:&quot;request&quot;,&quot;correlation&quot;:&quot;f56746d5-bb35-47ed-bda3-d4da00bd14ea&quot;,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;remote&quot;:&quot;0:0:0:0:0:0:0:1&quot;,&quot;method&quot;:&quot;POST&quot;,&quot;uri&quot;:&quot;http://localhost:8080/customers&quot;,&quot;headers&quot;:{&quot;accept&quot;:[&quot;*/*&quot;],&quot;accept-encoding&quot;:[&quot;gzip, deflate&quot;],&quot;accept-language&quot;:[&quot;de,en-US;q=0.7,en;q=0.3&quot;],&quot;connection&quot;:[&quot;keep-alive&quot;],&quot;content-length&quot;:[&quot;79&quot;],&quot;content-type&quot;:[&quot;application/json&quot;],&quot;dnt&quot;:[&quot;1&quot;],&quot;host&quot;:[&quot;localhost:8080&quot;],&quot;origin&quot;:[&quot;http://localhost:8080&quot;],&quot;referer&quot;:[&quot;http://localhost:8080/swagger-ui.html&quot;],&quot;user-agent&quot;:[&quot;Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0&quot;]},&quot;body&quot;:{&quot;id&quot;:0,&quot;firstName&quot;:&quot;Harry&quot;,&quot;lastName&quot;:&quot;Potter&quot;,&quot;email&quot;:&quot;...&quot;}}
	2020-04-22 20:22:08.769 ERROR [customer service,eea0fa6b27939e0e,eea0fa6b27939e0e,false] 6256 --- [nio-8080-exec-2] d.j.s.f.e.CustomExceptionHandler         : Input validation error: error code: Email error message: keine g&#252;ltige E-Mail-Adresse field: email
	2020-04-22 20:22:08.784  INFO [customer service,eea0fa6b27939e0e,eea0fa6b27939e0e,false] 6256 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;local&quot;,&quot;type&quot;:&quot;response&quot;,&quot;correlation&quot;:&quot;f56746d5-bb35-47ed-bda3-d4da00bd14ea&quot;,&quot;duration&quot;:62,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;status&quot;:400,&quot;headers&quot;:{&quot;Connection&quot;:[&quot;close&quot;],&quot;Content-Type&quot;:[&quot;application/json&quot;],&quot;Date&quot;:[&quot;Wed, 22 Apr 2020 18:22:08 GMT&quot;],&quot;trace-id&quot;:[&quot;eea0fa6b27939e0e&quot;],&quot;Transfer-Encoding&quot;:[&quot;chunked&quot;]},&quot;body&quot;:{&quot;errorList&quot;:[{&quot;timestamp&quot;:&quot;2020-04-22T18:22:08.769106500Z&quot;,&quot;code&quot;:&quot;Email&quot;,&quot;message&quot;:&quot;keine g&#195;&#188;ltige E-Mail-Adresse&quot;,&quot;parameter&quot;:&quot;email&quot;,&quot;details&quot;:null}]}}
</pre></code>

## HTTP Distributed tracing in multiple services with Spring Boot Sleuth
* Same traceid will be used if request is forwarded to other services using sleuth when using RestTemplate client in calling service. Add new rest controller to simulate both sides:
<pre><code>
@RestController
@RequestMapping(&quot;/test&quot;)
//Swagger
@Api(value=&quot;TestController for Distributed Tracing Test&quot;)
public class TestController 
{
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired private RestTemplate restTemplate; 
	
  	@Bean public RestTemplate getRestTemplate() { 
  		  return new RestTemplate(); 
  	} 

  	@RequestMapping(&quot;/test&quot;) public ResponseEntity home() { 
  	  logger.info(&quot;you called test&quot;); 
  	  return new ResponseEntity(&quot;Hello World&quot;, HttpStatus.OK); 
  	} 

  	@RequestMapping(&quot;/calltest&quot;) public String callHome() { 
  	  logger.info(&quot;calling test&quot;); 
  	  return restTemplate.getForObject(&quot;http://localhost:8080/test/test&quot;, String.class); 
  	}
}
</pre></code>

* run following URL in browser: http://localhost:8080/test/calltest
<br>Example:
<pre><code>
2020-04-22 20:49:50.066  INFO [customer service,c3f1723c45e00d41,c3f1723c45e00d41,false] 6256 --- [nio-8080-exec-1] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;remote&quot;,&quot;type&quot;:&quot;request&quot;,&quot;correlation&quot;:&quot;a73dda8f-a31d-4015-ac7e-87941e99f0bb&quot;,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;remote&quot;:&quot;0:0:0:0:0:0:0:1&quot;,&quot;method&quot;:&quot;GET&quot;,&quot;uri&quot;:&quot;http://localhost:8080/test/calltest&quot;,&quot;headers&quot;:{&quot;accept&quot;:[&quot;text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8&quot;],&quot;accept-encoding&quot;:[&quot;gzip, deflate&quot;],&quot;accept-language&quot;:[&quot;de,en-US;q=0.7,en;q=0.3&quot;],&quot;connection&quot;:[&quot;keep-alive&quot;],&quot;dnt&quot;:[&quot;1&quot;],&quot;host&quot;:[&quot;localhost:8080&quot;],&quot;upgrade-insecure-requests&quot;:[&quot;1&quot;],&quot;user-agent&quot;:[&quot;Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:75.0) Gecko/20100101 Firefox/75.0&quot;]}}
2020-04-22 20:49:50.066  INFO [customer service,c3f1723c45e00d41,c3f1723c45e00d41,false] 6256 --- [nio-8080-exec-1] d.j.s.f.restcontroller.TestController    : calling test
2020-04-22 20:49:50.066  INFO [customer service,c3f1723c45e00d41,a2c4053bc350890b,false] 6256 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;remote&quot;,&quot;type&quot;:&quot;request&quot;,&quot;correlation&quot;:&quot;39620707-f175-4fe2-a4d2-4fe1f6917f65&quot;,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;remote&quot;:&quot;127.0.0.1&quot;,&quot;method&quot;:&quot;GET&quot;,&quot;uri&quot;:&quot;http://localhost:8080/test/test&quot;,&quot;headers&quot;:{&quot;accept&quot;:[&quot;text/plain, application/json, application/*+json, */*&quot;],&quot;connection&quot;:[&quot;keep-alive&quot;],&quot;host&quot;:[&quot;localhost:8080&quot;],&quot;user-agent&quot;:[&quot;Java/11.0.5&quot;],&quot;x-b3-parentspanid&quot;:[&quot;c3f1723c45e00d41&quot;],&quot;x-b3-sampled&quot;:[&quot;0&quot;],&quot;x-b3-spanid&quot;:[&quot;a2c4053bc350890b&quot;],&quot;x-b3-traceid&quot;:[&quot;c3f1723c45e00d41&quot;]}}
2020-04-22 20:49:50.066  INFO [customer service,c3f1723c45e00d41,a2c4053bc350890b,false] 6256 --- [nio-8080-exec-2] d.j.s.f.restcontroller.TestController    : you called test
2020-04-22 20:49:50.082  INFO [customer service,c3f1723c45e00d41,c3f1723c45e00d41,false] 6256 --- [nio-8080-exec-1] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;local&quot;,&quot;type&quot;:&quot;response&quot;,&quot;correlation&quot;:&quot;a73dda8f-a31d-4015-ac7e-87941e99f0bb&quot;,&quot;duration&quot;:31,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;status&quot;:200,&quot;headers&quot;:{&quot;Connection&quot;:[&quot;keep-alive&quot;],&quot;Content-Length&quot;:[&quot;11&quot;],&quot;Content-Type&quot;:[&quot;text/html;charset=ISO-8859-1&quot;],&quot;Date&quot;:[&quot;Wed, 22 Apr 2020 18:49:50 GMT&quot;],&quot;Keep-Alive&quot;:[&quot;timeout=60&quot;]},&quot;body&quot;:&quot;Hello World&quot;}
2020-04-22 20:49:50.082  INFO [customer service,c3f1723c45e00d41,a2c4053bc350890b,false] 6256 --- [nio-8080-exec-2] org.zalando.logbook.Logbook              : {&quot;origin&quot;:&quot;local&quot;,&quot;type&quot;:&quot;response&quot;,&quot;correlation&quot;:&quot;39620707-f175-4fe2-a4d2-4fe1f6917f65&quot;,&quot;duration&quot;:15,&quot;protocol&quot;:&quot;HTTP/1.1&quot;,&quot;status&quot;:200,&quot;headers&quot;:{&quot;Connection&quot;:[&quot;keep-alive&quot;],&quot;Content-Length&quot;:[&quot;11&quot;],&quot;Content-Type&quot;:[&quot;text/plain;charset=ISO-8859-1&quot;],&quot;Date&quot;:[&quot;Wed, 22 Apr 2020 18:49:50 GMT&quot;],&quot;Keep-Alive&quot;:[&quot;timeout=60&quot;]},&quot;body&quot;:&quot;Hello World&quot;}
</pre></code>
<br>Log entries of calling service and called service contain same trace id c3f1723c45e00d41.

## Build Docker image, run in docker desktop and push to docker hub 
* Install and test Docker Desktop for Windows:
<br> download docker desktop from https://hub.docker.com/editions/community/docker-ce-desktop-windows
<br> check docker in console: docker --version
<br>start docker desktop

* Activate Hyper-V properties, if not already done by Docker installation. Same needed after Virtual Box usage
<br> Open Windows / Apps&Features Activate / Deactivate Windows features
<br> Activate Hyper-V --> Hyper-V-Services + Hyper-V-Hypervisor	
		
* Create community docker repository on Docker Hub
<br> Create account in https://hub.docker.com/ and confirm email by mail
<br> create private docker repository (one private repository possible for community account)
<br> copy push command after creation: 
<pre><code>
docker push &lt;username&gt;/&lt;repositoryname&gt;:&lt;tagname&gt;, 
e.g. docker push jo9999ki/firsttrial:tagname
</pre></code>

* Create a simple docker file "Dockerfile" in application root folder
<pre><code>
FROM adoptopenjdk/openjdk11:alpine-jre
<br>
ARG JAR_FILE=./target/*.jar
<br>
COPY ${JAR_FILE} /app.jar
<br>
ENTRYPOINT exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar 
</pre></code>
<br> Major impact for size of created image is the jdk image used in FROM ... statement (here reduced from 686 to 204 MB). See alternatives here: https://www.dropbox.com/s/19mhkxmix8iztx2/openjdk_tags_2019-06-24.csv?dl=0

* Build image from docker file: docker "build <username>/<repositoryname>:<tagname> ." e.g. "docker build --tag jo9999ki/firsttrial:v1 ."
<br>(Take care, that " ." is included)

* Check image size for created image: docker image ls

* Deploy and test image on Docker Desktop -internal and external port = 8080
<pre><code>
docker run -d -p 8080:8080 --rm --name &lt;tagname&gt; &lt;username&gt;/&lt;repositoryname&gt;:&lt;tagname&gt;
e.g. docker run -d -p 8080:8080 --rm --name v1 jo9999ki/firsttrial:v1 
</pre></code>		
<br>Test swagger file: http://localhost:8080/swagger-ui.html
<br> open local session for installed image: docker run -ti --entrypoint /bin/sh jo9999ki/firsttrial:v1
<br>Delete deployment: docker kill <tagname> -> docker kill v1

* Push image to Docker hub repository
<br> Login in with repository user: "docker login - u <username>", e.g. "docker login -u jo9999ki" --> enter password
<br> Push image: "docker push <username>/<repositoryname>:<tagname>", e.g. "docker push jo9999ki/firsttrial:v1"

## Install minikube and deploy image
### Reasons to install minikube on virtual box
* Kubernetes inside Docker Desktop does not include dashboard
* Minikube installation for driver docker has a bug, that after installing the service it cannot be reached outside minikube (connection refused)
* Alternative it hyper-v stopped after minikube: start with errors 

### Install kubectl:
* Download kubectl.exe
<br> curl -LO https://storage.googleapis.com/kubernetes-release/release/v1.18.0/bin/windows/amd64/kubectl.exe
* Add the path of kubectl.exe to PATH (instead of folder in c:/Users/<your profile> create folder like this: C:\Program Files\kubectl)
* Run following command in console: kubectl version --client
* Create kubernetes config file 
<br> 	create empty file kubeconfig in a not write protected folder, e.g. in your user folders
<br> 	create Windows variable KUBECONFIG with path INCLUDING file name: E.g. KUBECONFIG=c:\Users\jkirchner\minikube_config\kubeconfig

### Install minikube
* Stop docker desktop	
* Deactivate Hyper-V properties ...
<br> 	Windows / Apps&Features --> Activate / Deactivate Windows features
<br> 	--> Deactivate Hyper-V / Hyper-V-Hypervisor (no further attribute)
<br> 	--> Restart Windows
* Install VirtualBox
<br> Download from https://www.virtualbox.org/wiki/Downloads and install and run virtualbox
<br> Install Minikube - Download https://github.com/kubernetes/minikube/releases/latest/download/minikube-installer.exe and install

### Deploy image
* Swith kubectl context to minikube
<br> kubectl config get-contexts
<br> kubectl config use-context minikube

* Optionally delete existing minikube before new deployment
<br> minikube delete

* Deploy image in minikube
<br> minikube start --driver=virtualbox
<br>(if current version is buggy (e.g. 1.18.0 for Hype-V, not for virtual box) with version: minikube start --kubernetes-version v1.17.0 --driver=virtualbox)

* Create minikube secret to allow minikube to download image from your repository
<br> kubectl create secret docker-registry regcred --docker-server=https://index.docker.io/v1/ --docker-username=jo9999ki --docker-password=Werkbank#1 --docker-email=jochen_kirchner@yahoo.com 

* Create yaml file to download image and create 3 pods + service for external access with port 30001
<pre><code>
	apiVersion: apps/v1
	kind: Deployment
	metadata: 
	  name: v1
	spec: 
	  replicas: 3 
	  selector: 
		matchLabels: 
		  app: v1-label 
	  template: 
		metadata: 
		  labels: 
			app: v1-label 
		spec: 
		  containers: 
		  - name: v1-container 
			image: jo9999ki/firsttrial:v1
			ports: 
			- containerPort: 8080 
		  imagePullSecrets: 
		  - name: regcred 
	--- 
	apiVersion: v1
	kind: Service
	metadata: 
	  name: v1-service
	spec: 
	  selector: 
		app: v1-label
	  ports: 
	  - protocol: TCP 
		port: 8080 
		nodePort: 30001 
	  type: NodePort  
</pre></code>

* Update minikube with yaml file:
<br> kubectl apply -f C:\Users\jkirchner\minikube_config\firsttrial.yml

* Check download image is complete and container/pods are created
<br> kubectl get pods
<br> kubect describe pod <pod-name>

* Check service state and external port
<br> kubectl get services
<br> kubectl describe services/<service-name>

* Check outside access to app
<br> - Show service host and port: minikube service v1-service  --url
<br> - test app in console: curl <host>:<port>/customers
<br> - start app in browser: minikube service v1-service; enhance url: <host>:<port>/swagger-ui.html

* Show dashboard in browser
<br> minikube dashboard 
 
### Problem analysis
* Check service state: 
<br> --> kubectl get services
<br> --> kubectl describe services/<service-name>

* Check pod status
<br> --> kubectl get pods
<br> --> kubectl get pods -l app=v1
<br> --> kubectl describe pods

* Check app in single pod
<br> --> kubectl exec -ti <pod-name> curl localhost:8080/customers
<br> --> kubectl logs <pod-name>
<br> --> kubectl exec -ti <pod-name> bash
<br> 	--> work with UNIX commands (e.g. cat <filename> --> end bash with "exit"

* Check minikube logs
<br> --> minikube logs --problems		

### Scale deployment to amount of pods
<br> kubectl scale deployments/v1 --replicas=4
			
# Links
* [Spring Boot Actuator: Production-ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
* [Micrometer: Spring Boot 2's new application metrics collector](https://spring.io/blog/2018/03/16/micrometer-spring-boot-2-s-new-application-metrics-collector)
* [Micrometer Documentation](https://micrometer.io/docs)
* [Zalando Logbook for logging request and response content](https://github.com/zalando/logbook)
* [Spring Cloud Sleuth](https://cloud.spring.io/spring-cloud-sleuth/reference/html/)
* [Install Docker for windows](https://docs.docker.com/docker-for-windows/)
* [How to used docker for windows](https://docs.docker.com/machine/drivers/hyper-v/#usage)
* [Docker file reference](https://docs.docker.com/engine/reference/builder/)
* [Tutorials Kubernetes](https://kubernetes.io/de/docs/tutorials/#grundlagen)
* [Install Minikube](https://kubernetes.io/docs/tasks/tools/install-minikube/)
