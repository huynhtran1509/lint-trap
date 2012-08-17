lint-trap
=========

A maven plugin to run lint.

=========
usage
=========

Add the following to your pom file to run lint during a build.
	<plugin>
            <groupId>com.willowtreeapps.android</groupId>
            <artifactId>lint-trap-maven-plugin</artifactId>
            <version>*version*</version>
            <executions>
                <execution>
                    <phase>process-test-resources</phase>
                    <goals>
                        <goal>run-lint</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

If you would like more fine grained control then add some configuration options. The default is to;
-Run all of the lint checks
-Ignore any apklibs
-Report only errors 
-Fail the build on errors.

=========
Options
=========

*Checks*
The checks you want lint to do. If none are found then it does them all
<pre>
<configuration>
	<checks>
		<check>HardcodedText</check>
		<check>NewApi</check>
	</checks>
</configuration>	
</pre>

*Exclusions*
Defines the directories to ignore. Note that the check is a "startsWith" so it is not necessary to define the entire path.

<pre>
<configuration>
	<exclusions>
		<exclusion>target/unpack/apklibs/com.</exclusion>
	</exclusions>
</configuration>	
</pre>

*Minimum Error Level*
Defines the minimum error level which will fail a build. Possible values are Error,Warning, and Never.

<pre>
<configuration>
	<minErrorLevel>Error</minErrorLevel>
</configuration>	
</pre>

*Minimum Log Level*
Defines the minimum error level to log. Possible values are Error and Warning.

<pre>
<configuration>
	<minLogLevel>Error</minLogLevel>
</configuration>	
</pre>

*Complete example*
<pre>
<plugin>
  <groupId>com.willowtreeapps.android</groupId>
  <artifactId>lint-trap-plugin</artifactId>
  <version>1.0.0</version>
  <executions>
    <execution>
        <phase>process-test-resources</phase>
        <goals>
            <goal>run-lint</goal>
        </goals>
    </execution>
  </executions>
  <configuration>
    <checks>
        <check>HardcodedText</check>
        <check>NewApi</check>
    </checks>
    <exclusions>
        <exclusion>target/unpack/apklibs/com.</exclusion>
    </exclusions>
    <minErrorLevel>Error</minErrorLevel>
    <minLogLevel>Warning</minLogLevel>
  </configuration>
</plugin>
</pre>

===========
*Note for intellij users*
===========

intellij does not keep the .classpath file up to date which is what lint uses for parsing. Therefore you need to include this maven plugin as well:
<pre>
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-eclipse-plugin</artifactId>
  <version>2.9</version>
  <executions>
    <execution>
        <phase>compile</phase>
        <goals>
            <goal>eclipse</goal>
        </goals>
    </execution>
  </executions>
</plugin>
</pre>

===========
Resolving lint issues
===========
First import the annotations library:
<pre>
<dependency>
  <groupId>android</groupId>
  <artifactId>annotation</artifactId>
  <version>20</version>
</dependency>
</pre>

Then use the @SuppressLint() tag or the @TargetApi() call. *Note that this can only sit on a method call or a class definition.* So design your app with that in mind.
<pre>
@Override
@SuppressLint("NewApi")
public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("int");
        data = ObjectArbiter.filteredData.get(position);
        mIcon.onPopulateAccessibilityEvent(null);
}
</pre>
