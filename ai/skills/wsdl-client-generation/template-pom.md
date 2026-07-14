# Template POM — WSDL Client Module

Use this template for a SOAP client module inside a consuming microservice.

```xml
<project>
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>fr.francetv.myservice</groupId>
        <artifactId>myservice-parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>

    <artifactId>myservice-policy-soap-client</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>fr.francetv.foundation</groupId>
            <artifactId>foundation-soap-client-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.cxf</groupId>
                <artifactId>cxf-codegen-plugin</artifactId>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals><goal>wsdl2java</goal></goals>
                        <configuration>
                            <wsdlOptions>
                                <wsdlOption>
                                    <wsdl>${project.basedir}/src/main/resources/wsdl/policy-service.wsdl</wsdl>
                                    <extraargs>
                                        <extraarg>-p</extraarg>
                                        <extraarg>fr.francetv.policy.client.soap</extraarg>
                                    </extraargs>
                                </wsdlOption>
                            </wsdlOptions>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
