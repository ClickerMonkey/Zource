zource
======

![Stable](http://i4.photobucket.com/albums/y123/Freaklotr4/stage_stable.png)

A Java library for pooling expensive resources.

**Features**
- A ResourcePool can return reusable resources (Resource implementation dependent) or can generate new resources if the pool doesn't have any currently available.
- A ResourcePool can automatically downsize it's number of resources if the number of unused resources meets a specified number for a specified amount of time.

**Documentation**
- [JavaDoc](http://gh.magnos.org/?r=http://clickermonkey.github.com/Zource/)

**Example**

```java
// Create a factory that creates services for handling tasks.
ResourceFactory<TaskService> factory = new ResourceFactory<TaskService>() {
    public TaskService allocate() {
        TaskService s = new TaskService();
        s.start();
        return s;
    }
};

// Create a pool of services for handling tasks and populate it.
ResourcePool<TaskService> pool = new ResourcePool<TaskService>(factory);
pool.setMinCapacity(5);
pool.setMaxCapacity(10);
pool.setAllocateSize(5);
pool.setAllocateThreshold(50);
pool.setDeallocateSize(1);
pool.populate();

// Get a ready service to process a task
TaskService serve = pool.allocate();

// Create the task and give it to the service.
Task myTask = ...;
serve.addEvent(myTask);

// Empty pool and wait for all tasks to finish.
pool.empty();
```

**Builds**
- [zource-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Zource/blob/master/build/zource-1.0.0.jar?raw=true)
- [zource-src-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Zource/blob/master/build/zource-src-1.0.0.jar?raw=true) *- includes source code*
- [zource-all-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Zource/blob/master/build/zource-1.0.0.jar?raw=true) *- includes all dependencies*
- [zource-all-src-1.0.0.jar](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Zource/blob/master/build/zource-src-1.0.0.jar?raw=true) *- includes all dependencies and source code*

**Projects using zource:**
- [taskaroo](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Taskaroo)
- [falcon](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Falcon)

**Dependencies**
- [curity](http://gh.magnos.org/?r=https://github.com/ClickerMonkey/Curity)
