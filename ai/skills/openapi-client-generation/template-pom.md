# Template POM — OpenAPI Client Module

Use this template for a client module inside a consuming microservice.

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>fr.francetv.myservice</groupId>
        <artifactId>myservice-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>myservice-pricing-client</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>fr.francetv.foundation</groupId>
            <artifactId>foundation-http-client-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.openapitools</groupId>
                <artifactId>openapi-generator-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <goals><goal>generate</goal></goals>
                        <configuration>
                            <inputSpec>${project.basedir}/src/main/resources/openapi/pricing-api.yaml</inputSpec>
                            <generatorName>java</generatorName>
                            <library>webclient</library>
                            <apiPackage>fr.francetv.pricing.client.api</apiPackage>
                            <modelPackage>fr.francetv.pricing.client.model</modelPackage>
                            <generateApiTests>false</generateApiTests>
                            <generateModelTests>false</generateModelTests>
                            <configOptions>
                                <useJakartaEe>true</useJakartaEe>
                                <openApiNullable>false</openApiNullable>
                            </configOptions>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
