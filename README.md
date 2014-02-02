cas-server-support-redmine
==========================

RedmineDBのアカウント情報をCAS認証に使用するためのライブラリです。

手順
----

TOMCATのセットアップは終わっているものとします。

http://www.jasig.org/cas/download より最新版を取得
展開したディレクトリ内の
modules/cas-server-webapp-x.x.x.war
を $TOMCAT_HOME/webapps に展開
(x.x.x.xはダウンロードしたバージョン)

warファイルはzipなのでunzipコマンドで展開できます。

```
unzip cas-server-webapp-x.x.x.war -d cas
```

下記の4つのライブラリを
$TOMCAT_HOME/webapps/cas/WEB-INF/lib にコピーします

* cas-server-support-jdbc-x.x.x.jar
* commons-dbcp-1.4.jar
* commons-pool-1.6.jar
* mysql-connector-java-x.x.x.jar

cas-server-support-jdbc-x.x.x.jarは
cas-server-webapp-x.x.x.warがあったディレクトリにあります。
その他は http://search.maven.org/ などで探してダウンロードします。

本ライブラリを取得して同じく
$TOMCAT_HOME/webapps/cas/WEB-INF/lib に配置します。

```
wget https://raw.github.com/boushi-bird/cas-server-support-redmine/master/repo/cas-server-support-redmine/cas-server-support-redmine/0.0.2/cas-server-support-redmine-0.0.2.jar
mv cas-server-support-redmine-0.0.2.jar $TOMCAT_HOME/webapps/cas/WEB-INF/lib
```
cas-server-support-redmine-0.0.2.jar

設定ファイルを下記のように書き換えます。
$TOMCAT_HOME/webapps/cas/WEB-INF/deployerConfigContext.xml

```diff
-                                <bean
-                                        class="org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler" />
+                                <bean class="com.github.boushi_bird.cas.adaptors.jdbc.RedmineAuthenticationHandler">
+                                        <property name="dataSource" ref="dataSource" />
+                                        <!-- Redmine1.2.0より前の場合は下記設定が必要 -->
+                                        <!-- <property name="useSalt" value="false" /> -->
+                                </bean>

+        <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource">
+                <property name="driverClassName" value="com.mysql.jdbc.Driver" />
+                <property name="url" value="jdbc:mysql://localhost:3306/redmine" />
+                <property name="username" value="xxx" />
+                <property name="password" value="xxx" />
+        </bean>

```

TOMCATを起動します。
