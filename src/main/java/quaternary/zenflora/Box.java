package quaternary.zenflora;

public class Box<T> {
	public T thing;
	
	public T get() {
		return thing;
	}
	
	public void store(T thing) {
		this.thing = thing;
	}
}
