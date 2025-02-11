/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.studio.contract.core.mapping.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.studio.contract.core.mapping.operation.FieldToContractInputMappingBuilder.aRelationMapping;
import static org.bonitasoft.studio.contract.core.mapping.operation.FieldToContractInputMappingBuilder.aSimpleMapping;
import static org.bonitasoft.studio.model.businessObject.BusinessObjectBuilder.aBO;
import static org.bonitasoft.studio.model.businessObject.FieldBuilder.aCompositionField;
import static org.bonitasoft.studio.model.businessObject.FieldBuilder.aSimpleField;
import static org.bonitasoft.bpm.model.process.builders.BusinessObjectDataBuilder.aBusinessData;
import static org.bonitasoft.bpm.model.process.builders.ContractInputBuilder.aContractInput;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.bpm.model.util.ExpressionConstants;
import org.bonitasoft.studio.common.log.BonitaStudioLog;
import org.bonitasoft.studio.common.repository.AbstractRepository;
import org.bonitasoft.studio.common.repository.RepositoryAccessor;
import org.bonitasoft.studio.contract.core.mapping.FieldToContractInputMapping;
import org.bonitasoft.studio.contract.core.mapping.RelationFieldToContractInputMapping;
import org.bonitasoft.studio.contract.core.mapping.SimpleFieldToContractInputMapping;
import org.bonitasoft.studio.contract.core.mapping.operation.BusinessObjectInstantiationException;
import org.bonitasoft.studio.contract.core.mapping.operation.DefaultFormatterPreferences;
import org.bonitasoft.studio.expression.editor.ExpressionProviderService;
import org.bonitasoft.studio.groovy.BonitaScriptGroovyCompilationUnit;
import org.bonitasoft.studio.model.businessObject.BusinessObjectBuilder;
import org.bonitasoft.studio.model.businessObject.FieldBuilder.RelationFieldBuilder;
import org.bonitasoft.studio.model.businessObject.FieldBuilder.SimpleFieldBuilder;
import org.bonitasoft.bpm.model.expression.Expression;
import org.bonitasoft.bpm.model.expression.assertions.ExpressionAssert;
import org.bonitasoft.bpm.model.process.BusinessObjectData;
import org.bonitasoft.bpm.model.process.ContractInput;
import org.bonitasoft.bpm.model.process.ContractInputType;
import org.bonitasoft.bpm.model.process.assertions.ContractInputAssert;
import org.codehaus.groovy.eclipse.refactoring.formatter.DefaultGroovyFormatter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FieldToContractInputMappingExpressionBuilderTest {

    @Mock
    private RepositoryAccessor repositoryAccessor;
    @Mock
    private ExpressionProviderService expressionEditorService;
    @Mock
    private BonitaScriptGroovyCompilationUnit groovyCompilationUnit;

    @Test
    public void should_create_an_operation_for_a_given_complex_contact_input_and_a_composite_reference_business_data_field()
            throws Exception {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        final RelationField address = aCompositionField("address", aBO("Address").build());
        final FieldToContractInputMapping mapping = aRelationMapping(address).build();
        mapping.toContractInput(aContractInput().withName("employee").withType(ContractInputType.COMPLEX).build());
        final BusinessObjectData businessObjectData = aBusinessData().withName("myEmployee").build();
        final Expression expression = expressionBuilder.toExpression(businessObjectData,
                mapping, false, true);

        ExpressionAssert.assertThat(expression)
                .hasName("employee.address")
                .hasContent(format("if (!employee?.address) {"
                        + System.lineSeparator()
                        + "return null"
                         + System.lineSeparator()
                        + "}"
                         + System.lineSeparator()
                        + "def addressVar = myEmployee.address ?: new Address()"
                         + System.lineSeparator()
                        + "return addressVar"))
                .hasReturnType("Address")
                .hasType(ExpressionConstants.SCRIPT_TYPE);
        assertThat(expression.getReferencedElements()).hasSize(2);
    }

    private FieldToContractInputMappingExpressionBuilder newExpressionBuilder() throws JavaModelException {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = spy(
                new FieldToContractInputMappingExpressionBuilder(repositoryAccessor,
                        expressionEditorService));
        doReturn(groovyCompilationUnit).when(expressionBuilder).groovyCompilationUnit(any(Expression.class));
        return expressionBuilder;
    }

    @Test
    public void should_create_an_operation_for_a_given_simple_contact_input_and_a_primitive_business_data_field()
            throws Exception {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        final SimpleField lastNameField = aSimpleField().withName("lastName").ofType(FieldType.STRING).build();
        final FieldToContractInputMapping mapping = aSimpleMapping(lastNameField).build();
        final Expression expression = expressionBuilder.toExpression(aBusinessData().withName("myEmployee").build(),
                mapping, false, true);

        ExpressionAssert.assertThat(expression)
                .hasName("lastName")
                .hasContent("lastName")
                .hasType(ExpressionConstants.SCRIPT_TYPE);
        assertThat(expression.getReferencedElements()).hasSize(1);
    }

    @Test
    public void should_create_an_operation_for_a_given_complex_contact_input_and_a_primitive_business_data_field()
            throws Exception {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        final SimpleField lastNameField = aSimpleField().withName("lastName").ofType(FieldType.STRING).build();
        final FieldToContractInputMapping mapping = aSimpleMapping(lastNameField).build();
        mapping.toContractInput(aContractInput().withType(ContractInputType.COMPLEX)
                .withName("employee").build());

        final BusinessObjectData data = aBusinessData().withName("myEmployee").build();
        final Expression expression = expressionBuilder.toExpression(data,
                mapping, false, true);

        ExpressionAssert.assertThat(expression)
                .hasName("employee.lastName")
                .hasContent("employee?.lastName")
                .hasReturnType(String.class.getName())
                .hasType(ExpressionConstants.SCRIPT_TYPE);
        assertThat(expression.getReferencedElements()).hasSize(1);
        ContractInputAssert.assertThat((ContractInput) expression.getReferencedElements().get(0)).hasName("employee");
    }

    @Test
    public void should_not_add_businessVariable_dependency_forInitializationScript()
            throws JavaModelException, BusinessObjectInstantiationException {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        final RelationField address = aCompositionField("address", aBO("Address").build());
        final FieldToContractInputMapping mapping = aRelationMapping(address).build();
        mapping.toContractInput(aContractInput().withName("employee").withType(ContractInputType.COMPLEX).build());
        final BusinessObjectData businessObjectData = aBusinessData().withName("myEmployee").build();
        final Expression expression = expressionBuilder.toExpression(businessObjectData,
                mapping, true);

        ExpressionAssert.assertThat(expression)
                .hasName("initMyEmployee()")
                .hasContent(format("if (!employee?.address) {"
                        + System.lineSeparator()
                        + "return null"
                        + System.lineSeparator()
                        + "}"
                        + System.lineSeparator()
                        + "def addressVar = new Address()"
                        + System.lineSeparator()
                        + "return addressVar"))
                .hasReturnType("Address")
                .hasType(ExpressionConstants.SCRIPT_TYPE);
        assertThat(expression.getReferencedElements()).hasSize(1);
    }

    @Test
    public void should_delete_compilation_unit_after_dependencies_resolution() throws Exception {
        final FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        final SimpleField lastNameField = aSimpleField().withName("lastName").ofType(FieldType.STRING).build();
        final FieldToContractInputMapping mapping = aSimpleMapping(lastNameField).build();
        expressionBuilder.toExpression(aBusinessData().withName("myEmployee").build(),
                mapping, false);

        verify(groovyCompilationUnit).delete(eq(true), any());
    }

    @Test
    public void should_create_expression_for_multiple_business_data_with_a_multiple_complex_field_in_aggregation()
            throws Exception {
        RelationFieldToContractInputMapping bookMapping = createBookMapping(true, false);
        BusinessObjectData businessData = aBusinessData().withName("myBooks").multiple().build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Edit mode
        Expression expression = expressionBuilder.toExpression(businessData, bookMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = myBooks.find { it.persistenceId.toString() == currentBookInput.persistenceId_string} ?: new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            def pageList = []\n"
                + "            //For each item collected in multiple input\n"
                + "            currentBookInput.page.each{\n"
                + "                //Add Page instance\n"
                + "                pageList.add({ currentPageInput ->\n"
                + "                    def pageVar = pageDAO.findByPersistenceId(currentPageInput.persistenceId_string?.trim() ? currentPageInput.persistenceId_string.toLong() : null)\n"
                + "                    if(!pageVar) {\n"
                + "                        throw new IllegalArgumentException(\"The aggregated reference of type `Page` with the persistence id \" + currentPageInput.persistenceId_string?.trim() ? currentPageInput.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                + "                    }\n"
                + "                    pageVar.pageContent = currentPageInput.pageContent\n"
                + "                    return pageVar\n"
                + "                }(it))\n"
                + "            }\n"
                + "            return pageList}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList");

        // Create mode
        expression = expressionBuilder.toExpression(businessData, bookMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add a new composed Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            def pageList = []\n"
                + "            //For each item collected in multiple input\n"
                + "            currentBookInput.page.each{\n"
                + "                //Add Page instance\n"
                + "                pageList.add({ currentPageInput ->\n"
                + "                    def pageVar = pageDAO.findByPersistenceId(currentPageInput.persistenceId_string?.trim() ? currentPageInput.persistenceId_string.toLong() : null)\n"
                + "                    if(!pageVar) {\n"
                + "                        throw new IllegalArgumentException(\"The aggregated reference of type `Page` with the persistence id \" + currentPageInput.persistenceId_string?.trim() ? currentPageInput.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                + "                    }\n"
                + "                    pageVar.pageContent = currentPageInput.pageContent\n"
                + "                    return pageVar\n"
                + "                }(it))\n"
                + "            }\n"
                + "            return pageList}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList");
    }

    @Test
    public void should_create_expression_for_multiple_business_data_with_a_simple_complex_field_in_aggregation()
            throws Exception {
        RelationFieldToContractInputMapping bookMapping = createBookMapping(false, false);
        BusinessObjectData businessData = aBusinessData().withName("myBooks").multiple().build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Edit mode
        Expression expression = expressionBuilder.toExpression(businessData, bookMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = myBooks.find { it.persistenceId.toString() == currentBookInput.persistenceId_string} ?: new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            //Retrieve aggregated Page using its DAO and persistenceId\n"
                + "            def pageVar = pageDAO.findByPersistenceId(currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null)\n"
                + "            if (!pageVar) {\n"
                + "                if (currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null) {\n"
                + "                    throw new IllegalArgumentException(\"The aggregated reference of type `Page` with the persistence id \" + currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                + "                }\n"
                + "                return null\n"
                + "            }\n"
                + "            pageVar.pageContent = currentBookInput.page?.pageContent\n"
                + "            return pageVar}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList"));

        // Create mode
        expression = expressionBuilder.toExpression(businessData, bookMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add a new composed Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            //Retrieve aggregated Page using its DAO and persistenceId\n"
                + "            def pageVar = pageDAO.findByPersistenceId(currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null)\n"
                + "            if (!pageVar) {\n"
                + "                if (currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null) {\n"
                + "                    throw new IllegalArgumentException(\"The aggregated reference of type `Page` with the persistence id \" + currentBookInput.page?.persistenceId_string?.trim() ? currentBookInput.page.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                + "                }\n"
                + "                return null\n"
                + "            }\n"
                + "            pageVar.pageContent = currentBookInput.page?.pageContent\n"
                + "            return pageVar}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList"));
    }

    @Test
    public void should_create_expression_for_multiple_business_data_with_a_simple_complex_field_in_composition()
            throws Exception {
        RelationFieldToContractInputMapping bookMapping = createBookMapping(false, true);
        BusinessObjectData businessData = aBusinessData().withName("myBooks").multiple().build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Edit mode
        Expression expression = expressionBuilder.toExpression(businessData, bookMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = myBooks.find { it.persistenceId.toString() == currentBookInput.persistenceId_string} ?: new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            if (!currentBookInput.page) {\n"
                + "                return null\n"
                + "            }\n"
                + "            def pageVar = bookVar.page ?: new com.company.Page()\n"
                + "            pageVar.pageContent = currentBookInput.page?.pageContent\n"
                + "            return pageVar}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList"));

        // Create mode
        expression = expressionBuilder.toExpression(businessData, bookMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add a new composed Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            if (!currentBookInput.page) {\n"
                + "                return null\n"
                + "            }\n"
                + "            def pageVar = new com.company.Page()\n"
                + "            pageVar.pageContent = currentBookInput.page?.pageContent\n"
                + "            return pageVar}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList"));
    }

    @Test
    public void should_create_expression_for_multiple_business_data_with_a_multiple_complex_field_in_composition()
            throws Exception {
        RelationFieldToContractInputMapping bookMapping = createBookMapping(true, true);
        BusinessObjectData businessData = aBusinessData().withName("myBooks").multiple().build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Edit mode
        Expression expression = expressionBuilder.toExpression(businessData, bookMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = myBooks.find { it.persistenceId.toString() == currentBookInput.persistenceId_string} ?: new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            def pageList = []\n"
                + "            //For each item collected in multiple input\n"
                + "            currentBookInput.page.each{\n"
                + "                //Add a new composed Page instance\n"
                + "                pageList.add({ currentPageInput ->\n"
                + "                    def pageVar = bookVar.page?.find { it.persistenceId.toString() == currentPageInput.persistenceId_string } ?: new com.company.Page()\n"
                + "                    pageVar.pageContent = currentPageInput.pageContent\n"
                + "                    return pageVar\n"
                + "                }(it))\n"
                + "            }\n"
                + "            return pageList}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList");

        // Create mode
        expression = expressionBuilder.toExpression(businessData, bookMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace("def bookList = []\n"
                + "//For each item collected in multiple input\n"
                + "Book.each{\n"
                + "   //Add  a new composed Book instance\n"
                + "    bookList.add({ currentBookInput ->\n"
                + "        def bookVar = new com.company.Book()\n"
                + "        bookVar.page = {\n"
                + "            def pageList = []\n"
                + "            //For each item collected in multiple input\n"
                + "            currentBookInput.page.each{\n"
                + "                //Add a new composed Page instance\n"
                + "                pageList.add({ currentPageInput ->\n"
                + "                    def pageVar = new com.company.Page()\n"
                + "                    pageVar.pageContent = currentPageInput.pageContent\n"
                + "                    return pageVar\n"
                + "                }(it))\n"
                + "            }\n"
                + "            return pageList}()\n"
                + "        return bookVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return bookList");
    }

    private RelationFieldToContractInputMapping createBookMapping(boolean pageMultiple, boolean pageInComposition) {
        RelationField bookField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("Book")
                .multiple()
                .referencing(new BusinessObjectBuilder("com.company.Book").build())
                .build();
        RelationField pageField = RelationFieldBuilder.aRelationField()
                .withName("page")
                .referencing(new BusinessObjectBuilder("com.company.Page").build())
                .build();
        pageField.setType(pageInComposition ? Type.COMPOSITION : Type.AGGREGATION);
        if (pageMultiple) {
            pageField.setCollection(true);
        }

        SimpleField pageContentField = SimpleFieldBuilder.aStringField("pageContent").build();
        SimpleField persistenceIdBookField = SimpleFieldBuilder.aStringField("persistenceId_string").build();
        SimpleField persistenceIdPageField = SimpleFieldBuilder.aStringField("persistenceId_string").build();

        RelationFieldToContractInputMapping bookMapping = new RelationFieldToContractInputMapping(bookField);
        RelationFieldToContractInputMapping pageMapping = new RelationFieldToContractInputMapping(pageField);
        SimpleFieldToContractInputMapping pageContentMapping = new SimpleFieldToContractInputMapping(pageContentField);
        SimpleFieldToContractInputMapping persistenceIdBookMapping = new SimpleFieldToContractInputMapping(
                persistenceIdBookField);
        SimpleFieldToContractInputMapping persistenceIdPageMapping = new SimpleFieldToContractInputMapping(
                persistenceIdPageField);

        bookMapping.addChild(pageMapping);
        bookMapping.addChild(persistenceIdBookMapping);
        pageMapping.addChild(pageContentMapping);
        pageMapping.addChild(persistenceIdPageMapping);

        return bookMapping;
    }

    @Test
    public void should_create_expression_for_single_business_data_with_two_single_composition_layers() throws Exception {
        RelationField rootField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("rootInput")
                .referencing(new BusinessObjectBuilder("com.company.Root").build())
                .build();
        RelationField nodeField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("node")
                .referencing(new BusinessObjectBuilder("com.company.Node").build())
                .build();
        RelationField leafField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("leaf")
                .referencing(new BusinessObjectBuilder("com.company.Leaf").build())
                .build();
        SimpleField rootNameField = SimpleFieldBuilder.aStringField("rootName").build();
        SimpleField nodeNameField = SimpleFieldBuilder.aStringField("nodeName").build();
        SimpleField leafNameField = SimpleFieldBuilder.aStringField("leafName").build();

        RelationFieldToContractInputMapping rootMapping = new RelationFieldToContractInputMapping(rootField);
        RelationFieldToContractInputMapping nodeMapping = new RelationFieldToContractInputMapping(nodeField);
        RelationFieldToContractInputMapping leafMapping = new RelationFieldToContractInputMapping(leafField);
        SimpleFieldToContractInputMapping rootNameMapping = new SimpleFieldToContractInputMapping(rootNameField);
        SimpleFieldToContractInputMapping nodeNameMapping = new SimpleFieldToContractInputMapping(nodeNameField);
        SimpleFieldToContractInputMapping leafNameMapping = new SimpleFieldToContractInputMapping(leafNameField);

        leafMapping.addChild(leafNameMapping);
        nodeMapping.addChild(nodeNameMapping);
        nodeMapping.addChild(leafMapping);
        rootMapping.addChild(rootNameMapping);
        rootMapping.addChild(nodeMapping);

        BusinessObjectData businessData = aBusinessData().withName("myRoot").build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Create mode
        Expression expression = expressionBuilder.toExpression(businessData, rootMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def rootVar = new com.company.Root()\n"
                + "rootVar.rootName = rootInput?.rootName\n"
                + "rootVar.node = {\n"
                + "    if (!rootInput?.node) {\n"
                + "        return null\n"
                + "    }\n"
                + "    def nodeVar = new com.company.Node()\n"
                + "    nodeVar.nodeName = rootInput?.node?.nodeName\n"
                + "    nodeVar.leaf = {\n"
                + "        if (!rootInput?.node?.leaf) {\n"
                + "            return null\n"
                + "        }\n"
                + "        def leafVar = new com.company.Leaf()\n"
                + "        leafVar.leafName = rootInput?.node?.leaf?.leafName\n"
                + "        return leafVar}()\n"
                + "return nodeVar}()\n"
                + "return rootVar"));

        // Edite mode -> on the node mapping (operation setNode)
        expression = expressionBuilder.toExpression(businessData, nodeMapping, false);
        assertThat(expression.getContent())
                .isEqualToIgnoringWhitespace(format("if (!rootInput?.node) {\n"
                        + "    return null\n"
                        + "}\n"
                        + "def nodeVar = myRoot.node ?: new com.company.Node()\n"
                        + "nodeVar.nodeName = rootInput?.node?.nodeName\n"
                        + "nodeVar.leaf = {\n"
                        + "    if (!rootInput?.node?.leaf) {\n"
                        + "        return null\n"
                        + "    }\n"
                        + "    def leafVar = nodeVar.leaf ?: new com.company.Leaf()\n"
                        + "    leafVar.leafName = rootInput?.node?.leaf?.leafName\n"
                        + "    return leafVar}()\n"
                        + "return nodeVar"));
    }

    @Test
    public void should_create_expression_for_single_business_data_with_two_multiple_composition_layers() throws Exception {
        RelationField rootField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("rootInput")
                .referencing(new BusinessObjectBuilder("com.company.Root").build())
                .build();
        RelationField nodeField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("node")
                .multiple()
                .referencing(new BusinessObjectBuilder("com.company.Node").build())
                .build();
        RelationField leafField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("leaf")
                .multiple()
                .referencing(new BusinessObjectBuilder("com.company.Leaf").build())
                .build();
        SimpleField rootNameField = SimpleFieldBuilder.aStringField("rootName").build();
        SimpleField nodeNameField = SimpleFieldBuilder.aStringField("nodeName").build();
        SimpleField leafNameField = SimpleFieldBuilder.aStringField("leafName").build();
        SimpleField persistenceIdNodeField = SimpleFieldBuilder.aStringField("persistenceId_string").build();
        SimpleField persistenceIdLeafField = SimpleFieldBuilder.aStringField("persistenceId_string").build();

        RelationFieldToContractInputMapping rootMapping = new RelationFieldToContractInputMapping(rootField);
        RelationFieldToContractInputMapping nodeMapping = new RelationFieldToContractInputMapping(nodeField);
        RelationFieldToContractInputMapping leafMapping = new RelationFieldToContractInputMapping(leafField);
        SimpleFieldToContractInputMapping rootNameMapping = new SimpleFieldToContractInputMapping(rootNameField);
        SimpleFieldToContractInputMapping nodeNameMapping = new SimpleFieldToContractInputMapping(nodeNameField);
        SimpleFieldToContractInputMapping leafNameMapping = new SimpleFieldToContractInputMapping(leafNameField);
        SimpleFieldToContractInputMapping persistenceIdNodeMapping = new SimpleFieldToContractInputMapping(
                persistenceIdNodeField);
        SimpleFieldToContractInputMapping persistenceIdLeafMapping = new SimpleFieldToContractInputMapping(
                persistenceIdLeafField);

        leafMapping.addChild(leafNameMapping);
        leafMapping.addChild(persistenceIdLeafMapping);
        nodeMapping.addChild(nodeNameMapping);
        nodeMapping.addChild(persistenceIdNodeMapping);
        nodeMapping.addChild(leafMapping);
        rootMapping.addChild(rootNameMapping);
        rootMapping.addChild(nodeMapping);

        BusinessObjectData businessData = aBusinessData().withName("myRoot").build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Create mode
        Expression expression = expressionBuilder.toExpression(businessData, rootMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def rootVar = new com.company.Root()\n"
                + "rootVar.rootName = rootInput?.rootName\n"
                + "rootVar.node = {\n"
                + "    def nodeList = []\n"
                + "    //For each item collected in multiple input\n"
                + "    rootInput?.node.each{\n"
                + "        //Add a new composed Node instance\n"
                + "        nodeList.add({ currentNodeInput ->\n"
                + "            def nodeVar = new com.company.Node()\n"
                + "            nodeVar.nodeName = currentNodeInput.nodeName\n"
                + "            nodeVar.leaf = {\n"
                + "                def leafList = []\n"
                + "                //For each item collected in multiple input\n"
                + "                currentNodeInput.leaf.each{\n"
                + "                    //Add a new composed Leaf instance\n"
                + "                    leafList.add({ currentLeafInput ->\n"
                + "                        def leafVar = new com.company.Leaf()\n"
                + "                        leafVar.leafName = currentLeafInput.leafName\n"
                + "                        return leafVar\n"
                + "                    }(it))\n"
                + "                }\n"
                + "                return leafList}()\n"
                + "            return nodeVar\n"
                + "        }(it))\n"
                + "    }\n"
                + "    return nodeList}()\n"
                + "return rootVar"));

        // Edit mode -> on the node mapping (operation setNode)        
        expression = expressionBuilder.toExpression(businessData, nodeMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def nodeList = []\n"
                + "//For each item collected in multiple input\n"
                + "rootInput?.node.each{\n"
                + "    //Add Node instance\n"
                + "    nodeList.add({ currentNodeInput ->\n"
                + "        def nodeVar = myRoot.node.find { it.persistenceId.toString() == currentNodeInput.persistenceId_string} ?: new com.company.Node()\n"
                + "        nodeVar.nodeName = currentNodeInput.nodeName\n"
                + "        nodeVar.leaf = {\n"
                + "            def leafList = []\n"
                + "            //For each item collected in multiple input\n"
                + "            currentNodeInput.leaf.each{\n"
                + "                //Add a new composed Leaf instance\n"
                + "                leafList.add({ currentLeafInput ->\n"
                + "                    def leafVar = nodeVar.leaf?.find { it.persistenceId.toString() == currentLeafInput.persistenceId_string } ?: new com.company.Leaf()\n"
                + "                    leafVar.leafName = currentLeafInput.leafName\n"
                + "                    return leafVar\n"
                + "                }(it))\n"
                + "            }\n"
                + "        return leafList}()\n"
                + "        return nodeVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return nodeList"));
    }

    @Test
    public void should_create_expression_for_multiple_business_data_with_two_composition_layers() throws Exception {
        RelationField rootField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("rootInput")
                .multiple()
                .referencing(new BusinessObjectBuilder("com.company.Root").build())
                .build();
        RelationField nodeField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("node")
                .referencing(new BusinessObjectBuilder("com.company.Node").build())
                .build();
        RelationField leafField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("leaf")
                .multiple()
                .referencing(new BusinessObjectBuilder("com.company.Leaf").build())
                .build();
        SimpleField rootNameField = SimpleFieldBuilder.aStringField("rootName").build();
        SimpleField nodeNameField = SimpleFieldBuilder.aStringField("nodeName").build();
        SimpleField leafNameField = SimpleFieldBuilder.aStringField("leafName").build();
        SimpleField persistenceIdRootField = SimpleFieldBuilder.aStringField("persistenceId_string").build();
        SimpleField persistenceIdLeafField = SimpleFieldBuilder.aStringField("persistenceId_string").build();

        RelationFieldToContractInputMapping rootMapping = new RelationFieldToContractInputMapping(rootField);
        RelationFieldToContractInputMapping nodeMapping = new RelationFieldToContractInputMapping(nodeField);
        RelationFieldToContractInputMapping leafMapping = new RelationFieldToContractInputMapping(leafField);
        SimpleFieldToContractInputMapping rootNameMapping = new SimpleFieldToContractInputMapping(rootNameField);
        SimpleFieldToContractInputMapping nodeNameMapping = new SimpleFieldToContractInputMapping(nodeNameField);
        SimpleFieldToContractInputMapping leafNameMapping = new SimpleFieldToContractInputMapping(leafNameField);
        SimpleFieldToContractInputMapping persistenceIdRootMapping = new SimpleFieldToContractInputMapping(
                persistenceIdRootField);
        SimpleFieldToContractInputMapping persistenceIdLeafMapping = new SimpleFieldToContractInputMapping(
                persistenceIdLeafField);

        leafMapping.addChild(leafNameMapping);
        leafMapping.addChild(persistenceIdLeafMapping);
        nodeMapping.addChild(nodeNameMapping);
        nodeMapping.addChild(leafMapping);
        rootMapping.addChild(rootNameMapping);
        rootMapping.addChild(nodeMapping);
        rootMapping.addChild(persistenceIdRootMapping);

        BusinessObjectData businessData = aBusinessData().withName("myRoot").build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Create mode
        Expression expression = expressionBuilder.toExpression(businessData, rootMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def rootList = []\n"
                + "//For each item collected in multiple input\n"
                + "rootInput.each{\n"
                + "    //Add a new composed Root instance\n"
                + "    rootList.add({ currentRootInput ->\n"
                + "        def rootVar = new com.company.Root()\n"
                + "        rootVar.rootName = currentRootInput.rootName\n"
                + "        rootVar.node = {\n"
                + "            if (!currentRootInput.node) {\n"
                + "                return null\n"
                + "            }\n"
                + "            def nodeVar = new com.company.Node()\n"
                + "            nodeVar.nodeName = currentRootInput.node?.nodeName\n"
                + "            nodeVar.leaf = {\n"
                + "                def leafList = []\n"
                + "                //For each item collected in multiple input\n"
                + "                currentRootInput.node.leaf.each{\n"
                + "                    //Add a new composed Leaf instance\n"
                + "                    leafList.add({ currentLeafInput ->\n"
                + "                        def leafVar = new com.company.Leaf()\n"
                + "                        leafVar.leafName = currentLeafInput.leafName\n"
                + "                        return leafVar\n"
                + "                    }(it))\n"
                + "                }\n"
                + "                return leafList}()\n"
                + "            return nodeVar}()\n"
                + "        return rootVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return rootList"));

        // Edit mode    
        expression = expressionBuilder.toExpression(businessData, rootMapping, false);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def rootList = []\n"
                + "//For each item collected in multiple input\n"
                + "rootInput.each{\n"
                + "    //Add Root instance\n"
                + "    rootList.add({ currentRootInput ->\n"
                + "        def rootVar = myRoot.find { it.persistenceId.toString() == currentRootInput.persistenceId_string} ?: new com.company.Root()\n"
                + "        rootVar.rootName = currentRootInput.rootName\n"
                + "        rootVar.node = {\n"
                + "            if (!currentRootInput.node) {\n"
                + "                return null\n"
                + "            }\n"
                + "            def nodeVar = rootVar.node ?: new com.company.Node()\n"
                + "            nodeVar.nodeName = currentRootInput.node?.nodeName\n"
                + "            nodeVar.leaf = {\n"
                + "                def leafList = []\n"
                + "                //For each item collected in multiple input\n"
                + "                currentRootInput.node.leaf.each{\n"
                + "                    //Add a new composed Leaf instance\n"
                + "                    leafList.add({ currentLeafInput ->\n"
                + "                        def leafVar = nodeVar.leaf?.find { it.persistenceId.toString() == currentLeafInput.persistenceId_string } ?: new com.company.Leaf()\n"
                + "                        leafVar.leafName = currentLeafInput.leafName\n"
                + "                        return leafVar\n"
                + "                    }(it))\n"
                + "                }\n"
                + "                return leafList}()\n"
                + "            return nodeVar}()\n"
                + "        return rootVar\n"
                + "    }(it))\n"
                + "}\n"
                + "return rootList"));
    }

    @Test
    public void should_create_expression_for_single_business_data_with_a_simple_complex_field_in_aggregation()
            throws Exception {
        RelationField rootField = RelationFieldBuilder.aRelationField()
                .composition()
                .withName("rootInput")
                .referencing(new BusinessObjectBuilder("com.company.Root").build())
                .build();
        RelationField leafField = RelationFieldBuilder.aRelationField()
                .aggregation()
                .withName("leaf")
                .referencing(new BusinessObjectBuilder("com.company.Leaf").build())
                .build();
        SimpleField rootNameField = SimpleFieldBuilder.aStringField("rootName").build();
        SimpleField leafNameField = SimpleFieldBuilder.aStringField("leafName").build();
        SimpleField persistenceIdLeafField = SimpleFieldBuilder.aStringField("persistenceId_string").build();

        RelationFieldToContractInputMapping rootMapping = new RelationFieldToContractInputMapping(rootField);
        RelationFieldToContractInputMapping leafMapping = new RelationFieldToContractInputMapping(leafField);
        SimpleFieldToContractInputMapping rootNameMapping = new SimpleFieldToContractInputMapping(rootNameField);
        SimpleFieldToContractInputMapping leafNameMapping = new SimpleFieldToContractInputMapping(leafNameField);
        SimpleFieldToContractInputMapping persistenceIdLeafMapping = new SimpleFieldToContractInputMapping(
                persistenceIdLeafField);

        leafMapping.addChild(leafNameMapping);
        leafMapping.addChild(persistenceIdLeafMapping);
        rootMapping.addChild(rootNameMapping);
        rootMapping.addChild(leafMapping);

        BusinessObjectData businessData = aBusinessData().withName("myRoot").build();
        FieldToContractInputMappingExpressionBuilder expressionBuilder = newExpressionBuilder();

        // Create mode
        Expression expression = expressionBuilder.toExpression(businessData, rootMapping, true);
        assertThat(expression.getContent()).isEqualToIgnoringWhitespace(format("def rootVar = new com.company.Root()\n"
                + "rootVar.rootName = rootInput?.rootName\n"
                + "rootVar.leaf = {\n"
                + "    //Retrieve aggregated Leaf using its DAO and persistenceId\n"
                + "    def leafVar = leafDAO.findByPersistenceId(rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null)\n"
                + "    if (!leafVar) {\n"
                + "        if (rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null) {\n"
                + "            throw new IllegalArgumentException(\"The aggregated reference of type `Leaf` with the persistence id \" + rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                + "        }\n"
                + "        return null\n"
                + "    }\n"
                + "    leafVar.leafName = rootInput?.leaf?.leafName\n"
                + "    return leafVar}()\n"
                + "return rootVar"));

        // Edit mode -> setLeaf operation
        expression = expressionBuilder.toExpression(businessData, leafMapping, false);
        assertThat(expression.getContent())
                .isEqualToIgnoringWhitespace(format("//Retrieve aggregated Leaf using its DAO and persistenceId\n"
                        + "def leafVar = leafDAO.findByPersistenceId(rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null)\n"
                        + "if (!leafVar) {\n"
                        + "    if (rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null) {\n"
                        + "        throw new IllegalArgumentException(\"The aggregated reference of type `Leaf` with the persistence id \" + rootInput?.leaf?.persistenceId_string?.trim() ? rootInput.leaf.persistenceId_string.toLong() : null + \" has not been found.\")\n"
                        + "    }\n"
                        + "    return null\n"
                        + "}\n"
                        + "leafVar.leafName = rootInput?.leaf?.leafName\n"
                        + "return leafVar"));
    }

    private String format(String initialValue) {
        final Document document = new Document(initialValue);
        try {
            new DefaultGroovyFormatter(document, new DefaultFormatterPreferences(), 0).format().apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
            BonitaStudioLog.error("Failed to format generated script", e);
        }
        return document.get();
    }
}
