#### 一、Redis的Replication：

    这里首先需要说明的是，在Redis中配置Master-Slave模式真是太简单了。相信在阅读完这篇Blog之后你也可以轻松做到。这里我们还是先列出一些理论性的知识，后面给出实际操作的案例。
    下面的列表清楚的解释了Redis Replication的特点和优势。
    1). 同一个Master可以同步多个Slaves。
    2). Slave同样可以接受其它Slaves的连接和同步请求，这样可以有效的分载Master的同步压力。因此我们可以将Redis的Replication架构视为图结构。
    3). Master Server是以非阻塞的方式为Slaves提供服务。所以在Master-Slave同步期间，客户端仍然可以提交查询或修改请求。
    4). Slave Server同样是以非阻塞的方式完成数据同步。在同步期间，如果有客户端提交查询请求，Redis则返回同步之前的数据。
    5). 为了分载Master的读操作压力，Slave服务器可以为客户端提供只读操作的服务，写服务仍然必须由Master来完成。即便如此，系统的伸缩性还是得到了很大的提高。
    6). Master可以将数据保存操作交给Slaves完成，从而避免了在Master中要有独立的进程来完成此操作。
    
#### 二、Replication的工作原理：

    在Slave启动并连接到Master之后，它将主动发送一个SYNC命令。此后Master将启动后台存盘进程，同时收集所有接收到的用于修改数据集的命令，在后台进程执行完毕后，Master将传送整个数据库文件到Slave，以完成一次完全同步。而Slave服务器在接收到数据库文件数据之后将其存盘并加载到内存中。此后，Master继续将所有已经收集到的修改命令，和新的修改命令依次传送给Slaves，Slave将在本次执行这些数据修改命令，从而达到最终的数据同步。
    如果Master和Slave之间的链接出现断连现象，Slave可以自动重连Master，但是在连接成功之后，一次完全同步将被自动执行。
    
#### 三、如何配置Replication：

    见如下步骤：
    1). 同时启动两个Redis服务器，可以考虑在同一台机器上启动两个Redis服务器，分别监听不同的端口，如6379和6380。
    2). 在Slave服务器上执行一下命令：
    /> redis-cli -p 6380   #这里我们假设Slave的端口号是6380
    redis 127.0.0.1:6380> slaveof 127.0.0.1 6379 #我们假设Master和Slave在同一台主机，Master的端口为6379
    OK
    上面的方式只是保证了在执行slaveof命令之后，redis_6380成为了redis_6379的slave，一旦服务(redis_6380)重新启动之后，他们之间的复制关系将终止。
    如果希望长期保证这两个服务器之间的Replication关系，可以在redis_6380的配置文件中做如下修改：
    /> cd /etc/redis  #切换Redis服务器配置文件所在的目录。
    /> ls
    6379.conf  6380.conf
    /> vi 6380.conf
    将
    # slaveof <masterip> <masterport>
    改为
    slaveof 127.0.0.1 6379
    保存退出。
    这样就可以保证Redis_6380服务程序在每次启动后都会主动建立与Redis_6379的Replication连接了。
    
#### 四、应用示例：

    这里我们假设Master-Slave已经建立。
    #启动master服务器。
    [root@Stephen-PC redis]# redis-cli -p 6379
    redis 127.0.0.1:6379>
    #情况Master当前数据库中的所有Keys。
    redis 127.0.0.1:6379> flushdb
    OK
    #在Master中创建新的Keys作为测试数据。
    redis 127.0.0.1:6379> set mykey hello
    OK
    redis 127.0.0.1:6379> set mykey2 world
    OK
    #查看Master中存在哪些Keys。
    redis 127.0.0.1:6379> keys *
    1) "mykey"
    2) "mykey2"
    
    #启动slave服务器。
    [root@Stephen-PC redis]# redis-cli -p 6380
    #查看Slave中的Keys是否和Master中一致，从结果看，他们是相等的。
    redis 127.0.0.1:6380> keys *
    1) "mykey"
    2) "mykey2"
    
    #在Master中删除其中一个测试Key，并查看删除后的结果。
    redis 127.0.0.1:6379> del mykey2
    (integer) 1
    redis 127.0.0.1:6379> keys *
    1) "mykey"
    
    #在Slave中查看是否mykey2也已经在Slave中被删除。
    redis 127.0.0.1:6380> keys *
    1) "mykey"