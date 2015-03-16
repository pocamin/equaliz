Equalize the class was created in the same philosophy as the class Mockito. Create a specific language for a daunting but critical task.


Equalize easily create methods equals, hashcode and clone with the same definition of equality.

# Create the methods #



To use Equaliz lets define simple hashCode() clone() et hashmap()


## clone ##
```
@Override
protected Object clone() {
    return Equaliz.clone(this);
}
```


## hashCode ##

```
@Override
public int hashCode() {
   return Equaliz.hashCode(this);
}
```

## Equals ##

```
@Override
public boolean equals(Object obj) {
   return Equaliz.equals(this, obj);
}    
```


# Define Equality #

## Pojo ##

Creates a static block Equaliz for each class

```
public class EgEqualiz
    static (
        / / Block equalize
        Equaliz.with (ExempleEqualiz.class). GetName ();
        Equaliz.with (ExempleEqualiz.class). GetTitle ();

    )
```


Equalize will identify that name and title will be used for equals,hashcode and clone methods


Of course we can make things much more complex


`Equaliz.with (Car.class). GetPassenger (). GetGrandMother.getDog.getName ();`


Equaliz identify the equality of two cars if the name of the dog of the grandmother of the passenger who drove this cars are the same.


## Collection ##

We can also compare elements from a collection

In the case of a simple collection (collection of primitive or String or Class)

```
Equaliz.with (Car.class). GetModelNames ();
```


In the case of a collection of pojo

```
Equaliz.withElementOf (Equaliz.with (Car.class). GetModel ()). GetName ();
```


In this case, restrictions are :

- We can not use collection collection.

- Must have completed a collection of simple generic definitions (Collection <? Extends String> will not work)


# limitations #



- We can only use POJOs, primitives, String and collections.

- For collections only collections of primitives, String, Date and pojo are supported.

- For collections, it must be typed with generics.

- We can not Equaliz final method.

- There is no question of using Equalize when performance counts. The ad hoc methods are several times faster than Equaliz. The processing time is still very low

- Equalize generates classes on the fly. We must be vigilant on the PermGen Space.

