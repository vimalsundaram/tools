import java.util.*;
import java.lang.reflect.*;
interface Function<A, B, X extends Throwable> {
  public B apply(A x) throws X;
}
class Functions {
  public static <A, B, X extends Throwable>
  List<B> applyAll(Function<A, B, X> f, List<A> list) throws X {
    List<B> result = new ArrayList<B>(list.size());
    for (A x : list) result.add(f.apply(x));
    return result;
  }
  public static void main(String[] args) {
    Function<String, Integer, Error> length =
      new Function<String, Integer, Error>() {
        public Integer apply(String s) {
          return s.length();
        }
      };
    Function<String, Class<?>, ClassNotFoundException> forName =
      new Function<String, Class<?>, ClassNotFoundException>() {
        public Class<?> apply(String s)
          throws ClassNotFoundException {
          return Class.forName(s);
        }
      };
    Function<String, Method, Exception> getRunMethod =
      new Function<String, Method, Exception>() {
        public Method apply(String s)
          throws ClassNotFoundException,NoSuchMethodException {
          return Class.forName(s).getMethod("run");
        }
      };
    List<String> strings = Arrays.asList(args);
    System.out.println(applyAll(length, strings));

    try { System.out.println(applyAll(forName, strings)); }
    catch (ClassNotFoundException e) { System.out.println(e); }

    try { System.out.println(applyAll(getRunMethod, strings)); }
    catch (ClassNotFoundException e) { System.out.println(e); }
    catch (NoSuchMethodException e) { System.out.println(e); }
    catch (RuntimeException e) { throw e; }
    catch (Exception e) { throw new AssertionError(); }
  }
}

