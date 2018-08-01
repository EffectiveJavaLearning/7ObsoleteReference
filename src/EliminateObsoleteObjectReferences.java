import cache.SocketCache;

/**
 * 相较C或者C艹这种需要手动回收资源的语音，Java的GC(Gabage Collector)帮我们偷了个大懒，
 * 对象用完之后会被GC自动回收。但这并不意味着不需要我们考虑内存管理的事情了。
 * 请看一个内存泄漏的例子{@link MemoryLeakTest}，通过将栈内引用置空，防止内存泄漏，避免对性能产生影响。
 *
 * 这样做的另一个好处是，可以防止使用其他对象时不小心使用到了这个本应被抛弃的对象，
 * 出现这种情况时会立即抛出空指针，而不是等我们注意到结果和预期不一致，再去辛苦地debug
 *
 * 需要注意的是，我们并不需要过分地小心，而对每个对象使用结束后都把它置空，这会把代码搞得很乱。
 * 清空过时对象引用应该是一种例外，而不是规范。消除过时对象引用的最好方式是让包含该对象的变量结束生命周期，
 * 只要变量声明的作用域范围设计得当，这种情况就会自然而然地发生。
 *
 * 那么什么时候需要考虑使用置空引用来防止内存泄漏？分析{@link MemoryLeakTest}中造成内存泄漏的原因，
 * 该类选择使用Stack自己管理内存，存储池包含了elements数组（对象引用单元，而不是对象本身，类比c语言指针）
 * 的元素，而不是对象本身，数组中，下标号小于size的元素有效，而大于的则无效。但是GC并不知道这一点，
 * 它把整个数组的元素全部等同对待。这时候，需要程序员将这种情况告诉GC，即手动置空数组元素。
 *
 * 所以导出了情形1：类自己管理内存时，程序员应当警惕内存泄漏。元素被释放掉时，该元素中的任何对象引用都要释放掉。
 *
 * 情形2：缓存。对象被放到缓存中后，很容易被忘掉。解决的办法是确定它何时才不再有意义，这时候就把它的对象引用清掉。
 * 根据具体情况有不同的解决方案（详见{@link SocketCache}）：
 *  (1).只要在缓存之外存在对某个项的键的引用，该项就有意义；如果没有存在对某个项的键的引用，
 *  该项就没有意义。这时使用WeakHashMap来代表缓存，在外部手动取消对其引用，让GC自动将其回收
 *  (2).通过LinkedHashMap自带的removeEldestEntry实现在添加新条目时顺便清理
 *
 *  情形3：监听器等回调。Android中这种情况非常常见，因为网络通信时需要时间的，经常会有数据没返回回来，
 *  用户就点了返回把界面finish掉，而由于未与负责数据返回结果的监听器解除绑定，最后弹出空指针。
 *  这种情况可以考虑对监听器使用弱引用，也可以使用RxJava等设计好的工具，这里一并省略
 *
 * 内存泄漏这种事通常不容易被察觉到，往往会在系统中存在很久很久，只有仔细地检查代码，或者借助内存泄漏分析工具，
 * 才能够发现它。所以我们最好能在设计的时候就预知并阻止它的发生。
 *
 * @author LightDance
 */
public class EliminateObsoleteObjectReferences {


    public static void main(String[] args) {
        //可以用debug对比数组中元素的状况
        MemoryLeakTest stack = new MemoryLeakTest();
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.badPop();
        stack.badPop();
        stack.badPop();
        stack.badPop();
        stack.badPop();
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.push(new Object());
        stack.pop();

    }

}
