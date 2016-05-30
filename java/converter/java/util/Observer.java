package java.util;
public interface Observer<S extends Observable<S,O,A>,
                          O extends Observer<S,O,A>,
                          A>
{
     public void update(S o, A a);
}

