package hardcoded.compiler.expression;

import java.util.List;

@Deprecated
public interface IStatement {
	public List<IStatement> getElements();
	public boolean hasElements();
}
