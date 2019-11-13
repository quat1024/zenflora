package quaternary.zenflora.annotation;

import vazkii.botania.api.subtile.SubTileEntity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Extends {
	Class<? extends SubTileEntity> value();
}
