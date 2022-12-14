package org.n52.sta.config;

import org.n52.grammar.StaPathGrammar;
import org.n52.grammar.StaPathLexer;
import org.n52.sta.http.util.path.PathFactory;
import org.n52.sta.http.util.path.StaPathVisitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("sta")
public class StaWebConfiguration {

    @Bean
    public PathFactory pathFactory() {
        return new PathFactory(StaPathGrammar::new, StaPathVisitor::new, StaPathLexer::new);
    }
}
