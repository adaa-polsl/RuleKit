package adaa.analytics.rules.rm.example.set;

public abstract class AbstractExampleReader implements IExampleReader {
    public AbstractExampleReader() {
    }

    public void remove() {
        throw new UnsupportedOperationException("The 'remove' operation is not supported by ExampleReaders!");
    }
}
