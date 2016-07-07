abstract class SimpleTree<E> {
  abstract public String toString();
  abstract public Double sum();
  public static <E> SimpleTree<E> leaf(final E e) {
    return new SimpleTree<E>() {
      public String toString() {
        return e.toString();
      }
      public Double sum() {
        return ((Number)e).doubleValue();
      }
    };
  }
  public static <E> SimpleTree<E> branch(final Tree<E> l, final Tree<E> r) {
    return new SimpleTree<E>() {
      public String toString() {
        return "("+l.toString()+"^"+r.toString()+")";
      }
      public Double sum() {
        return l.sum() + r.sum();
      }
    };
  }
}
class SimpleTreeClient {
  public static void main(String[] args) {
    SimpleTree<Integer> t =
      SimpleTree.branch(Tree.branch(Tree.leaf(1),
                              SimpleTree.leaf(2)),
                  SimpleTree.leaf(3));
    assert t.toString().equals("((1^2)^3)");
    assert t.sum() == 6;
  }
}

