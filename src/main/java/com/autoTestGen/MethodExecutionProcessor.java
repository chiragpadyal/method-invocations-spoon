package com.autoTestGen;

// import org.apache.logging.log4j.LogManager;
// import org.apache.logging.log4j.Logger;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.filter.AbstractFilter;
import spoon.support.QueueProcessingManager;
import spoon.support.reflect.declaration.CtMethodImpl;
 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodExecutionProcessor extends AbstractProcessor<CtMethodImpl<?>> {
    private Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> callList = new HashMap<>();
    // private static Logger logger = LoggerFactory.getLogger(MethodExecutionProcessor.class);

    @Override
    public void process(CtMethodImpl<?> ctMethod) {
        List<CtElement> elements = ctMethod.getElements(new AbstractFilter<CtElement>(CtElement.class) {
            @Override
            public boolean matches(CtElement ctElement) {
                return ctElement instanceof CtAbstractInvocation;
            }
        });
        List<CtExecutableReference<?>> calls = new ArrayList<>();
        for (CtElement element : elements) {
            CtAbstractInvocation<?> invocation = (CtAbstractInvocation<?>) element;
            calls.add(invocation.getExecutable());

        }
        callList.put(ctMethod.getReference(), calls);
    }


    Map<CtExecutableReference<?>, List<CtExecutableReference<?>>> executeSpoon(QueueProcessingManager queueProcessingManager) throws Exception {
        queueProcessingManager.addProcessor(this);
        queueProcessingManager.process(queueProcessingManager.getFactory().Package().getAll());
        // logger.debug("Method calls: " + callList);
        return callList;
    }
}

