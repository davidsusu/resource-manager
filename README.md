# Resource manager for Java

This library provides some useful utilities
for managing resources in a dynamic way.

You can register any resource-like object and its dependencies in a `ResourceManager`.
The manager ensures that all the transitive dependencies
will be opened before the requested resource, in the proper order.
For resources, it is not necessary to implement
the `Closeable` or `AutoCloseable` interface,
you can specify any custom closer for each.

Dynamic configuration handling is also supported.
Each configuration entry is a resource, and can have dependants.
When a configuration value is changed,
all its transitive dependants that are already open
will be closed (and then reinitialized) on-the-fly.
Configuration can be reloaded at any time,
you are free to implement your own and independent reload scheduling logic.
