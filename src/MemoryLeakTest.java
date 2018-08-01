import java.util.Arrays;
import java.util.EmptyStackException;

/**
 * 这是一个没有考虑内存管理而导致内存泄漏的例子.
 * 这一段程序虽然没有明显错误，但是却隐藏着一个问题：如果这个栈先增长后收缩，
 * 那么从栈中弹出来的对象不会被当作辣鸡处理，就算使用栈的程序不再引用这些对象也是如此，
 * 这是因为栈内部维护着对这些对象的过期引用(obsolete reference)，即永远也不会再被解除的引用。
 * 在这个例子中，由于pop出栈之后{@link #badPop()}，栈内原位置的元素仍然存在（因为用的是数组），
 * 过期引用指的是下标大于size的那一部分元素。
 *
 * 内存泄漏随着程序的运行，所导致的性能降低会越来越明显。在极端情况下，内存泄漏会导致磁盘交换，
 * 甚至程序失败，但这种极端情况下的错误较为少见。
 *
 * Java这种支持辣鸡回收的语言，内存泄漏（或称“无意识的对象保持”？）往往比较隐蔽，
 * 如果某对象被无意识地保存起来，那么GC就不会处理这个对象，也同样不会处理被这个对象所引用的其他对象。
 * 因此即使只有少量几个对象被无意识地保存下来，也会造成很多对象令GC无法处理，对性能造成巨大的潜在隐患。
 *
 * 本例中的改正措施十分简单，{@link #pop()}对应元素出栈后，将栈内引用去掉就好(elements[size] = null;)
 *
 *
 * @author LightDance
 */
public class MemoryLeakTest {
    private Object [] elements;
    private int size = 0;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public MemoryLeakTest(){
        elements = new Object[DEFAULT_INITIAL_CAPACITY];
    }

    public void push(Object obj){
        ensureCapacity();
        elements[size++] = obj;;
    }
    @Deprecated
    public Object badPop(){
        if (size == 0){
            throw new EmptyStackException();
        }
        return elements[--size];
    }

    public Object pop(){
        if (size == 0){
            throw new EmptyStackException();
        }
        Object result = elements[--size];
        //解除对过期对象的引用，防止内存泄漏
        elements[size] = null;
        return result;
    }

    private void ensureCapacity(){
        if (elements.length == size){
            elements = Arrays.copyOf(elements , 2*size + 1);
        }
    }
}
