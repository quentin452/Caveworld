package caveworld.util;

import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ArrayListExtended<E> extends ArrayList<E>
{
	public ArrayListExtended()
	{
		super();
	}

	public ArrayListExtended(Collection<? extends E> c)
	{
		super(c);
	}

	public boolean addIfAbsent(E value)
	{
		return value != null && !contains(value) && add(value);
	}

	public boolean addObject(Object obj)
	{
		return obj != null && add((E)obj);
	}

	public ArrayListExtended<E> addAllObject(Collection c)
	{
		for (Object obj : c.toArray())
		{
			addObject(obj);
		}

		return this;
	}

	public ArrayListExtended<E> addAllObject(Iterable iterable)
	{
		for (Object obj : iterable)
		{
			addObject(obj);
		}

		return this;
	}

	public ArrayListExtended<E> addAllObject(E... objects)
	{
		for (Object obj : objects)
		{
			addObject(obj);
		}

		return this;
	}

	public E get(int index, E value)
	{
		return index < 0 || index >= size() || get(index) == null ? value : get(index);
	}

	public ArrayListExtended<E> swap(int index1, int index2)
	{
		if (index1 >= 0 && index1 < size() && index2 >= 0 && index2 < size())
		{
			Collections.swap(this, index1, index2);
		}

		return this;
	}

	public ArrayListExtended<E> swapTo(int index, int amount)
	{
		return swap(index, MathHelper.clamp_int(index + amount, 0, size() - 1));
	}
}
