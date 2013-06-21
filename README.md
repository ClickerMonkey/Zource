zource
======

A Java library for pooling expensive resources.

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
- [zource-1.0.0.jar](https://github.com/ClickerMonkey/zource/blob/master/build/zource-1.0.0.jar?raw=true)
- [zource-src-1.0.0.jar](https://github.com/ClickerMonkey/zource/blob/master/build/zource-src-1.0.0.jar?raw=true) *- includes source code*
- [zource-all-1.0.0.jar](https://github.com/ClickerMonkey/zource/blob/master/build/zource-1.0.0.jar?raw=true) *- includes all dependencies*
- [zource-all-src-1.0.0.jar](https://github.com/ClickerMonkey/zource/blob/master/build/zource-src-1.0.0.jar?raw=true) *- includes all dependencies and source code*

**Projects using zource:**
- [taskaroo](https://github.com/ClickerMonkey/taskaroo)
- [falcon](https://github.com/ClickerMonkey/falcon)

**Dependencies**
- [curity](https://github.com/ClickerMonkey/curity)
