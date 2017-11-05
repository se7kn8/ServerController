package sebe3012.servercontroller.gui.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;

/**
 * Created by Sebe3012 on 21.04.2017.
 * A entry for the structure view
 */
public interface TreeEntry<T> {

	@NotNull
	String getName();

	@Nullable
	default Node getGraphic(){
		return null;
	}

	default boolean onDoubleClick(){
		//Do nothing by default
		return false;
	}

	@Nullable
	default ContextMenu getContextMenu(){
		//Do nothing by default
		return null;
	}

	@NotNull
	T getItem();

	void setItem(@NotNull T item);
}
