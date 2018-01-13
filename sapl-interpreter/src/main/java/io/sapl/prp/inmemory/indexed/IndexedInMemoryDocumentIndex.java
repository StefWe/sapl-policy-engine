package io.sapl.prp.inmemory.indexed;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.interpreter.PolicyEvaluationException;
import io.sapl.api.interpreter.SAPLInterpreter;
import io.sapl.api.pdp.Request;
import io.sapl.api.prp.InMemoryDocumentIndex;
import io.sapl.api.prp.ParsedDocumentPolicyRetrievalPoint;
import io.sapl.api.prp.PolicyRetrievalResult;
import io.sapl.grammar.sapl.SAPL;
import io.sapl.interpreter.DefaultSAPLInterpreter;
import io.sapl.interpreter.functions.FunctionContext;

public class IndexedInMemoryDocumentIndex implements InMemoryDocumentIndex {
	private static final SAPLInterpreter INTERPRETER = new DefaultSAPLInterpreter();

	ParsedDocumentPolicyRetrievalPoint index = new FastParsedDocumentIndex();
	Map<String, SAPL> parsedDocuments = new ConcurrentHashMap<>();

	@Override
	public void insert(String documentKey, String document) throws PolicyEvaluationException {
		parsedDocuments.put(documentKey, INTERPRETER.parse(document));
	}

	@Override
	public void publish(String documentKey) {
		index.put(documentKey, parsedDocuments.get(documentKey));
	}

	@Override
	public PolicyRetrievalResult retrievePolicies(Request request, FunctionContext functionCtx,
			Map<String, JsonNode> variables) {
		return index.retrievePolicies(request, functionCtx, variables);
	}

	@Override
	public void unpublish(String documentKey) {
		index.remove(documentKey);
	}

	@Override
	public void updateFunctionContext(FunctionContext functionCtx) {
		index.updateFunctionContext(functionCtx);
	}

	@Override
	public void setLiveMode() {
		// TODO Auto-generated method stub
	}

}
