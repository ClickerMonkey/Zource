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
ResourcePool<TaskService> pool = new ResourcePool<Resource>(factory);
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

https://github.com/ClickerMonkey/zource/tree/master/build
