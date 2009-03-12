/**
 * Copyright (c) 2000-2007, Shakarchi Asaf
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package velo.utils;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;


public class SetAsList {
	
	public static List asList(Collection c, int size) {
		if (c instanceof List && c instanceof RandomAccess)
			return (List) c;
		
		if (c.isEmpty())
			return Collections.EMPTY_LIST;
		
		return new ListImpl(c, size);
	}
	
	public static List asList(Collection c) {
		if (c == null) {
			return null;
		}
		return asList(c, c.size());
	}
	
	@SuppressWarnings("unchecked")
	static private class ListImpl extends AbstractList {
		
		private final Collection _c;
		private final int _bufferSize;
		private final int _cSize;
		private int _offset;
		private List _buffer;
		
		public ListImpl(Collection c, int size) {
			_c = c;
			_cSize = c.size();
			_bufferSize = size == 0 ? _cSize : Math.min(size, _cSize);
			_buffer = new ArrayList(_bufferSize);
			_offset = -1;
		}

		@Override
		public Object get(int index) {
			if (index < 0 || index >= _cSize) 
				throw new IndexOutOfBoundsException();
			
			int offset = (index / _bufferSize) * _bufferSize;
			if (offset != _offset) {
				_loadBuffer(offset);
				_offset = offset;
			}
			return _buffer.get(index  - _offset);
		}

		private void _loadBuffer(int offset) {
			Iterator it = _c.iterator();
			int i = 0;
			
			while (i < offset) {
				assert it.hasNext();
				it.next();
				i++;
			}
			
			_buffer.clear();
			
			int count = 0;
			while (count < _bufferSize && i < _cSize) {
				assert it.hasNext();
				_buffer.add(it.next());
				i++;
				count++;
			}
		}

		@Override
		public int size() {
			return _cSize;
		}
	}
}