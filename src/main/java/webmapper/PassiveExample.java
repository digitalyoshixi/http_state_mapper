/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package webmapper;

import java.util.Arrays;
import java.util.Collection;

import de.learnlib.algorithm.PassiveLearningAlgorithm.PassiveDFALearner;
import de.learnlib.algorithm.rpni.BlueFringeRPNIDFA;
import net.automatalib.alphabet.Alphabet;
import net.automatalib.alphabet.impl.Alphabets;
import net.automatalib.automaton.fsa.DFA;
import net.automatalib.graph.Graph;
import net.automatalib.incremental.dfa.tree.IncrementalDFATreeBuilder;
import net.automatalib.ts.DeterministicTransitionSystem;
import net.automatalib.ts.UniversalDTS;
import net.automatalib.visualization.Visualization;
import net.automatalib.word.Word;


/**
 * Example of setting up a passive DFA learner.
 */
public final class PassiveExample {

    private PassiveExample() {
        // prevent instantiation
    }

    public static void main(String[] args) {

        // define the alphabet
        final Alphabet<Character> alphabet = Alphabets.characters('a', 'd');

        Graph<Character,Character> graph = generate_Graph(alphabet, getPositiveTrainingSamples(), getNegativeTrainingSamples());

        Visualization.visualize(graph);

        // with negative samples (i.e. words that must not be accepted by the model) we get a more "realistic"
        // generalization of the given training set
        final DFA<?, Character> secondModel =
            computeModel(alphabet, getPositiveTrainingSamples(), getNegativeTrainingSamples());
        Visualization.visualize(secondModel, alphabet);
    }

    /**
     * Returns the positives samples from the example of chapter 12.4.3 of "Grammatical Inference" by Colin de la
     * Higuera.
     *
     * @return a collection of (positive) training samples
     */
    private static Collection<Word<Character>> getPositiveTrainingSamples() {
        return Arrays.asList(Word.fromString("abc"),
                             Word.fromString("a"),
                             Word.fromString("b"),
                             Word.fromString("c"),
                             Word.fromString("ab")
                             );
    }

    /**
     * Returns the negative samples from the example of chapter 12.4.3 of "Grammatical Inference" by Colin de la
     * Higuera.
     *
     * @return a collection of (negative) training samples
     */
    private static Collection<Word<Character>> getNegativeTrainingSamples() {
        return Arrays.asList(Word.fromString("abcd"),
                             Word.fromString("d"),
                             Word.fromString("bc")
                             );
    }
    
    private static <I> Graph<I,I> generate_Graph(Alphabet<I> alphabet,
                                              Collection<Word<I>> positiveSamples,
                                              Collection<Word<I>> negativeSamples) {
        
        IncrementalDFATreeBuilder<I> treeBuilder = new IncrementalDFATreeBuilder<>(alphabet);
        for (Word i : positiveSamples) {
            treeBuilder.insert(i, true);
        }
        for (Word i : negativeSamples) {
            treeBuilder.insert(i, false);
        }

        // get a usable view
        return (Graph<I, I>) treeBuilder.asGraph();
        // or, if you want a DFA-shaped transition system:

    }


    /**
     * Creates the learner instance, computes and return the inferred model.
     *
     * @param alphabet
     *         domain from which the learning data are sampled
     * @param positiveSamples
     *         positive samples
     * @param negativeSamples
     *         negative samples
     * @param <I>
     *         input symbol type
     *
     * @return the learned model
     */
    private static <I> DFA<?, I> computeModel(Alphabet<I> alphabet,
                                              Collection<Word<I>> positiveSamples,
                                              Collection<Word<I>> negativeSamples) {

        // instantiate learner
        // alternatively one can also use the EDSM variant (BlueFringeEDSMDFA from the learnlib-rpni-edsm artifact)
        // or the MDL variant (BlueFringeMDLDFA from the learnlib-rpni-mdl artifact)
        final PassiveDFALearner<I> learner = new BlueFringeRPNIDFA<>(alphabet);

        learner.addPositiveSamples(positiveSamples);
        learner.addNegativeSamples(negativeSamples);

        return learner.computeModel();
    }

}