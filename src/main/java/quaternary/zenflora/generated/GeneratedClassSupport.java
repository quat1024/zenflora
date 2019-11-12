package quaternary.zenflora.generated;

import quaternary.zenflora.templates.GenericFlowerTemplate;

import java.util.HashMap;
import java.util.Map;

public class GeneratedClassSupport {
	private static Map<String, GenericFlowerTemplate> links = new HashMap<>();
	
	public static void storeTemplate(String name, GenericFlowerTemplate flowerTemplate) {
		links.put(name, flowerTemplate);
	}
	
	public static GenericFlowerTemplate retrieveTemplate(String name) {
		return links.get(name);
	}
}
