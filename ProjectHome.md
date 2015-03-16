The main issues with hashcode and equals methods are the complexity to create methods that respect specification in case of complex objects.
Some library exists (equalsBuilder, HashCodeBuilder) but don't solve the main issue.

Equaliz, permits to define with a simple DSL (domain specific language) in one place what is object equality. Then you create in one line equals, hashcode and clone methods even if the original object is complexe.

# Changes #

16/08/2010
- Equaliz is 27x faster (using javassist to generate fast equals classes)
- Equaliz is no longer fully static


# Create the methods #



To use Equaliz lets define simple hashCode() clone() et hashmap()



## hashCode ##

```
@Override
public int hashCode() {
   return equalizer.hashCode(this);
}
```

## Equals ##

```
@Override
public boolean equals(Object obj) {
   return equalizer.equals(this, obj);
}    
```


# Define Equality #

## Pojo ##

Creates a static block Equaliz for each class

```
public class EgEqualiz


    static Equalizer equalizer;

    static (
        / / Block equalize
        Equaliz.with(ExempleEqualiz.class).getName ();
        Equaliz.with(ExempleEqualiz.class).getTitle ();
        
        // create an equalizer to use in equals and hashcode method
        equalizer = Equaliz.createEqualizer(EgEqualiz.class);
    )
```


Equalize will identify that name and title will be used for equals,hashcode and clone methods


Of course we can make things much more complex


`Equaliz.with(Car.class).getPassenger().getGrandMother.getDog.getName();`


Equaliz identify the equality of two cars if the name of the dog of the grandmother of the passenger who drove this cars are the same.


## Collection ##

We can also compare elements from a collection

In the case of a simple collection (collection of primitive or String or Class)

```
Equaliz.with(Car.class).getModelNames ();
```


In the case of a collection of pojo

```
Equaliz.withElementOf(Equaliz.with(Car.class).getModel()).getName();
```


In this case, restrictions are :

- We can not use collection collection.

- Must have completed a collection of simple generic definitions (Collection <? Extends String> will not work)


# limitations #



- We can only use POJOs, primitives, String and collections.

- For collections only collections of primitives, String, Date and pojo are supported.

- For collections, it must be typed with generics.

- We can not Equaliz final method.

- Equalize generates classes on the fly. We must be vigilant on the PermGen Space.