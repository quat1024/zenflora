package quaternary.zenflora;

import crafttweaker.api.data.IData;

public class Util {
	public static void writeDataOnto(IData src, IData dst) {
		src.asMap().forEach(dst::memberSet);
	}
}
