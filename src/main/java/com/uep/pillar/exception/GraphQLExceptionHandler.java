package com.uep.pillar.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * GraphQL exception handler for graphql-java-kickstart.
 * 
 * Note: graphql-java-kickstart automatically converts exceptions thrown in resolvers
 * to GraphQL errors. This class serves as documentation and can be extended if custom
 * error formatting is needed in the future.
 * 
 * Exceptions thrown in GraphQL resolvers are automatically wrapped in GraphQLError
 * by the library. The GlobalExceptionHandler handles REST endpoints.
 * 
 * For custom error formatting, exceptions should be handled in resolvers or
 * use graphql-java's error handling mechanisms.
 */
@Component
@Slf4j
public class GraphQLExceptionHandler {
    
    // GraphQL errors are automatically handled by graphql-java-kickstart
    // Exceptions thrown in resolvers are converted to GraphQL error responses
    // For custom error formatting, consider handling errors directly in resolvers
    // or using graphql-java's GraphQLErrorHandler interface if needed
}
