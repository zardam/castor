/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Intalio, Inc.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Intalio, Inc. Exolab is a registered
 *    trademark of Intalio, Inc.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY INTALIO, INC. AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * INTALIO, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999-2003 (C) Intalio, Inc. All Rights Reserved.
 *
 * This file was originally developed by Keith Visco during the
 * course of employment at Intalio Inc.
 * All portions of this file developed by Keith Visco after Jan 19 2005 are
 * Copyright (C) 2005 Keith Visco. All Rights Reserved.
 *
 * $Id$
 */
package org.exolab.castor.builder;

//--Binding file imports
import org.exolab.castor.builder.binding.ExtendedBinding;
import org.exolab.castor.builder.binding.PackageType;
import org.exolab.castor.builder.binding.PackageTypeChoice;
import org.exolab.castor.builder.binding.BindingException;
import org.exolab.castor.builder.binding.BindingLoader;
import org.exolab.castor.builder.binding.XMLBindingComponent;
import org.exolab.castor.builder.binding.types.BindingType;

//--Castor SOM import
import org.exolab.castor.xml.schema.reader.*;
import org.exolab.castor.xml.schema.*;

import org.exolab.javasource.*;

//--Utils imports
import org.exolab.castor.builder.util.ConsoleDialog;
import org.exolab.castor.util.Configuration;
import org.exolab.castor.util.LocalConfiguration;
import org.exolab.castor.util.NestedIOException;
import org.exolab.castor.util.Version;

import org.exolab.castor.mapping.xml.MappingRoot;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.XMLException;
import org.xml.sax.*;

//--Java IO imports
import java.io.FileOutputStream;
import java.io.Reader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

//--Java util imports
import java.util.Enumeration;
import java.util.Vector;
import java.util.Properties;

/**
 * A Java Source generation tool which uses XML Schema definitions
 * to create an Object model.
 *
 * @author <a href="mailto:kvisco@intalio.com">Keith Visco</a> - Main author.
 * @author <a href="mailto:blandin@intalio.com">Arnaud Blandin</a> - Contributions.
 * @author <a href="mailto:nsgreen@thazar.com">Nathan Green</a> - Contributions.
 * @version $Revision$ $Date: 2006-03-30 14:58:45 -0700 (Thu, 30 Mar 2006) $
**/
public class SourceGenerator extends BuilderConfiguration {
    //-------------/
    //- Constants -/
    //-------------/
    /**
     * The application name
    **/
    static final String APP_NAME = "Castor";

    /**
     * The application description
    **/
    static final String APP_DESC = "XML data binder for Java";

    /**
     * The application version
    **/
    static final String VERSION = Version.VERSION;

    /**
     * The application URI
    **/
    static final String APP_URI = "http://www.castor.org";

    /**
     * Warning message to remind users to create source
     * code for imported schema.
    **/
    private static final String IMPORT_WARNING
        = "Warning: Do not forget to generate source code for the following imported schema: ";

    /**
     * Castor configuration
     */
    private final Configuration _config;

    /**
     * The XMLBindingComponent used to create Java classes from an XML Schema
     */
    private final XMLBindingComponent _bindingComponent;

    //----------------------/
    //- Instance Variables -/
    //----------------------/

    private boolean _suppressNonFatalWarnings = false;

    /** Determines whether or not to print extra messages. */
    private boolean _verbose = false;

    /** A flag indicating whether or not to create
     *  descriptors for the generated classes. */
    private boolean _createDescriptors = true;

    /** A flag indicating whether or not to generate sources
     *  for imported XML Schemas. */
    private boolean _generateImported = false;

    /** The field info factory. */
    private final FieldInfoFactory _infoFactory;

    /** The source factory. */
    private SourceFactory _sourceFactory = null;

    private final ConsoleDialog _dialog;

    /** A vector that keeps track of all the schemas processed. */
    private Vector _schemasProcessed = null;

    /** A flag to indicate that the mapping file should be generated. */
    private boolean _generateMapping = false;

    /** The name of the mapping file to create used with the gen-mapping flag. */
    private String  _mappingFilename = "mapping.xml";

    /** A flag indicating whether or not to generate XML marshalling
     *  framework specific methods. */
    private boolean _createMarshalMethods = true;

    /** A flag indicating whether or not to implement CastorTestable
     *  (used by the Castor Testing Framework). */
    private boolean _testable = false;

    /** A flag indicating that SAX1 should be used when generating the source. */
    private boolean _sax1 = false;

    /** A flag indicating that enumerated types should be constructed to perform
     *  case insensitive lookups based on the values. */
    private boolean _caseInsensitive = false;

    private final SingleClassGenerator _singleClassGenerator;

    /**
     * Creates a SourceGenerator using the default FieldInfo factory
     */
    public SourceGenerator() {
        this(null);
    } //-- SourceGenerator

    /**
     * Creates a SourceGenerator using the specific field info Factory.
     *
     * @param infoFactory the FieldInfoFactory to use.
     */
    public SourceGenerator(FieldInfoFactory infoFactory) {
        this(infoFactory, null);
    }

    /**
     * Creates a SourceGenerator using the specific field info Factory and the
     * given Binding element.
     *
     * @param infoFactory the FieldInfoFactory to use.
     * @param binding the binding element to use.
     */
    public SourceGenerator(FieldInfoFactory infoFactory, ExtendedBinding binding) {
        super();

        _config = LocalConfiguration.getInstance();
        _dialog = new ConsoleDialog();
        _infoFactory = (infoFactory == null) ? new FieldInfoFactory() : infoFactory;

        load();

        _singleClassGenerator = new SingleClassGenerator(_dialog, this);
        _bindingComponent = new XMLBindingComponent(this);
        //--set the binding
        setBinding(binding);
    } //-- SourceGenerator

    public void setMappingFilename(String filename) {
        _mappingFilename = filename;
    }

    /**
     * Creates Java Source code (Object model) for the given XML Schema.
     *
     * @param schema the XML schema to generate the Java sources for.
     * @param packageName the package for the generated source files.
     */
    public void generateSource(Schema schema, String packageName) throws IOException {
        // by this time the properties have been set. if the sourceFactory
        // is null then create one using this for configuration. if this is
        // done before reading in the configuration there is a problem (CASTOR-1346)
        if (_sourceFactory == null) {
            _sourceFactory = new SourceFactory(this, _infoFactory);
            _sourceFactory.setCreateMarshalMethods(_createMarshalMethods);
            _sourceFactory.setTestable(_testable);
            _sourceFactory.setSAX1(_sax1);
            _sourceFactory.setCaseInsensitive(_caseInsensitive);
        }

        if (schema == null) {
            String err = "The argument 'schema' must not be null.";
            throw new IllegalArgumentException(err);
        }

        //-- reset the vector, most of the time only one schema to process
        SGStateInfo sInfo = new SGStateInfo(schema, this);
        //--make sure the XML Schema is valid
        try {
            schema.validate();
        } catch (ValidationException ve) {
            String err = "The schema:"+schema.getSchemaLocation()+" is not valid.\n";
            err += ve.getMessage();
            throw new IllegalArgumentException(err);
        }

        //--map the schemaLocation of the schema with the packageName defined
        if (packageName != null) {
            setLocationPackageMapping(schema.getSchemaLocation(), packageName);
        }

        sInfo.packageName = packageName;
        sInfo.setDialog(_dialog);
        sInfo.setVerbose(_verbose);
        sInfo.setSuppressNonFatalWarnings(_suppressNonFatalWarnings);

        createClasses(schema, sInfo);

        //-- TODO Cleanup integration :
        if (!_createDescriptors && _generateMapping) {
            String pkg = (packageName != null) ? packageName : "";
            MappingRoot mapping = sInfo.getMapping(pkg);
            if (mapping != null) {
                FileWriter writer = new FileWriter(_mappingFilename);
                Marshaller mars = new Marshaller(writer);
                mars.setSuppressNamespaces(true);
                try {
                    mars.marshal(mapping);
                }
                catch(Exception ex) {
                    throw new NestedIOException(ex);
                }
                writer.flush();
                writer.close();
            }
        }
        //--reset the vector of schemas processed
        _schemasProcessed = null;
    } //-- generateSource

    /**
     * Creates Java Source code (Object model) for the given XML Schema
     *
     * @param source - the InputSource representing the XML schema.
     * @param packageName the package for the generated source files
     */
    public void generateSource(InputSource source, String packageName) throws IOException {
        // -- get default parser from Configuration
        Parser parser = null;
        try {
            parser = _config.getParser();
        } catch(RuntimeException rte) {}
        if (parser == null) {
            _dialog.notify("fatal error: unable to create SAX parser.");
            return;
        }

        SchemaUnmarshaller schemaUnmarshaller = null;
        try {
           schemaUnmarshaller = new SchemaUnmarshaller();
        } catch (XMLException e) {
            //--The default constructor cannot throw
            //--exception so this should never happen
            //--just log the exception
            e.printStackTrace();
            System.exit(1);
        }

        Sax2ComponentReader handler = new Sax2ComponentReader(schemaUnmarshaller);
        parser.setDocumentHandler(handler);
        parser.setErrorHandler(handler);

        try {
            parser.parse(source);
        } catch(java.io.IOException ioe) {
            _dialog.notify("error reading XML Schema file");
            //throw ioe;
            return; // FIXME:  Replace with previous line
        } catch(org.xml.sax.SAXException sx) {

            Exception except = sx.getException();
            if (except == null) except = sx;

            if (except instanceof SAXParseException) {
                SAXParseException spe = (SAXParseException)except;
                _dialog.notify("SAXParseException: " + spe);
                _dialog.notify(" - occured at line ");
                _dialog.notify(Integer.toString(spe.getLineNumber()));
                _dialog.notify(", column ");
                _dialog.notify(Integer.toString(spe.getColumnNumber()));
            } else {
                except.printStackTrace();
            }
            //throw new RuntimeException(sx);
            return; // FIXME:  Replace with previous line
        }

        Schema schema = schemaUnmarshaller.getSchema();
        generateSource(schema, packageName);
    } //-- generateSource

    /**
     * Creates Java Source code (Object model) for the given XML Schema.
     *
     * @param reader the Reader with which to read the XML Schema definition.
     * The caller should close the reader, since thie method will not do so.
     * @param packageName the package for the generated source files
    **/
    public void generateSource(Reader reader, String packageName) throws IOException {
        InputSource source = new InputSource(reader);
        generateSource(source, packageName);
    } //-- generateSource

    /**
     * Creates Java Source code (Object model) for the given XML Schema.
     *
     * @param filename the full path to the XML Schema definition
     * @param packageName the package for the generated source files
    **/
    public void generateSource(String filename, String packageName)
        throws FileNotFoundException, IOException
    {
        //--basic cleanup
        if (filename.startsWith("./")) {
            filename = filename.substring(2);
        }
        FileReader reader = new FileReader(filename);
        InputSource source = new InputSource(reader);
        source.setSystemId(toURIRepresentation((new File(filename)).getAbsolutePath()));
        generateSource(source, packageName);
        try {
            reader.close();
        } catch(java.io.IOException iox) {}


    } //-- generateSource

    /**
     * Returns the version number of this SourceGenerator
     *
     * @return the version number of this SourceGenerator
    **/
    public static String getVersion() {
        return VERSION;
    } //-- getVersion

    /**
     * Set to true if SAX1 should be used in the marshall method
     */
    public void setSAX1(boolean sax1) {
        _sax1 = sax1;
    }

    /**
     * Set to true if enumerated type lookups should be performed in a case
     * insensitive manner.
     *
     * @param caseInsensitive when true, enumerated type lookups will be
     *        performed in a case insensitive manner.
     */
    public void setCaseInsensitive(final boolean caseInsensitive) {
        _caseInsensitive = caseInsensitive;
    }

    public void setSuppressNonFatalWarnings(boolean suppress) {
        _singleClassGenerator.setPromptForOverwrite(!suppress);
        _suppressNonFatalWarnings = suppress;
    } //-- setSuppressNonFatalWarnings

    /**
     * Sets whether or not the source code generator prints
     * additional messages during generating source code
     * @param verbose a boolean, when true indicates to
     * print additional messages
    **/
    public void setVerbose(boolean verbose) {
        _verbose = verbose;
    } //-- setVerbose

    /**
     * Sets whether or not to create ClassDescriptors for
     * the generated classes. By default, descriptors are
     * generated.
     *
     * @param createDescriptors a boolean, when true indicates
     * to generated ClassDescriptors
     *
    **/
    public void setDescriptorCreation(boolean createDescriptors) {
        _createDescriptors = createDescriptors;
        _singleClassGenerator.setDescriptorCreation(createDescriptors);
    } //-- setDescriptorCreation

    /**
     * Sets the destination directory.
     *
     * @param destDir the destination directory.
     */
    public void setDestDir(String destDir) {
        _singleClassGenerator.setDestDir(destDir);
    }

    /**
     * Sets whether or not to create the XML marshalling framework specific
     * methods (marshall, unmarshall, validate) in the generated classes.
     * By default, these methods are generated.
     *
     * @param createMarshalMethods a boolean, when true indicates
     * to generated the marshalling framework methods
     *
     */
    public void setCreateMarshalMethods(boolean createMarshalMethods) {
        _createMarshalMethods = createMarshalMethods;
    } //-- setCreateMarshalMethods

    /**
     * Sets whether or not to generate Java sources for imported XML Schema.
     * By default Java sources for imported XML schemas are not generated.
     *
     * @param generate true to generate the java classes for the imported XML Schema
     */
    public void setGenerateImportedSchemas(boolean generate) {
        _generateImported = generate;
    }

    /**
     * Sets whether or not a mapping file should be generated, this
     * is false by default. Note that this will only be used
     * when generation of descriptors has been disabled.
     *
     * @param generateMapping a flag that indicates whether or
     * not a mapping file should be generated.
     */
    public void setGenerateMappingFile(boolean generateMapping)
    {
        _generateMapping = generateMapping;
    } //-- setGenerateMappingFile

   /**
     * Sets whether or not to implement CastorTestable
     *
     * @param testable a boolean, when true indicates
     * to implement CastorTestable
     */
    public void setTestable(boolean testable) {
        _testable = testable;
    } //-- setTestable

   /**
    * Sets the binding to use with this instance of the SourceGenerator.
    *
    * @param binding the binding to use, null indicates that the default
    * binding will be used.
    */
    public void setBinding(ExtendedBinding binding) {
        if (binding != null) {
            processNamespaces(binding.getPackage());
        }
        //--initialize the XMLBindingComponent
        _bindingComponent.setBinding(binding);
    } //-- setBinding

   /**
    * Sets the binding to use given the path name of a Castor Binding File.
    *
    * @param fileName the file that represents a Binding
    */
    public void setBinding(String fileName) {
        try {
            ExtendedBinding binding = BindingLoader.createBinding(fileName);
            setBinding(binding);
        } catch (BindingException e) {
           //log these messages
            String err= "unable to load a binding file due to the following:\n";
            err +=e.getMessage();
            err += "\nThe Source Generator will continue with no binding file.";
            _dialog.notify(err);
        }
    }

   /**
    * Sets the binding to use given an InputSource identifying
    * a Castor Binding File.
    *
    * @param source an InputSource identifying a Castor Binding File.
    */
    public void setBinding(InputSource source) {
        try {
            ExtendedBinding binding = BindingLoader.createBinding(source);
            setBinding(binding);
        } catch (BindingException e) {
           //log these messages
            String err= "unable to load a binding file due to the following:\n";
            err +=e.getMessage();
            err += "\nThe Source Generator will continue with no binding file.";
            _dialog.notify(err);
        }
    }

    /**
     * Sets the line separator to use when printing the source code
     * @param lineSeparator the line separator to use when printing
     * the source code. This method is useful if you are generating
     * source on one platform, but will be compiling the source
     * on a different platform.
     * <BR />
     * <B>Note:</B>This can be any string, so be careful. I recommend
     * either using the default or using one of the following:<BR />
     * <PRE>
     *   windows systems use: "\r\n"
     *   unix systems use: "\n"
     *   mac systems use: "\r"
     * </PRE>
    **/
    public void setLineSeparator(String lineSeparator) {
        _singleClassGenerator.setLineSeparator(lineSeparator);
    } //-- setLineSeparator

    //-------------------/
    //- Private Methods -/
    //-------------------/

    private void createClasses(Schema schema, SGStateInfo sInfo)
        throws IOException
    {
        //-- ** print warnings for imported schemas **
        if (!_suppressNonFatalWarnings || _generateImported) {
            Enumeration enumeration = schema.getImportedSchema();
            while (enumeration.hasMoreElements()) {
                Schema importedSchema = (Schema)enumeration.nextElement();
                if (!_generateImported) {
                    System.out.println();
                    System.out.println(IMPORT_WARNING +
                        importedSchema.getSchemaLocation());
                } else {
                    if (_schemasProcessed == null)
                        _schemasProcessed = new Vector(7);
                    _schemasProcessed.add(schema);
                    if (!_schemasProcessed.contains(importedSchema)) {
                        SGStateInfo importedSInfo = new SGStateInfo(importedSchema, this);
                        importedSInfo.packageName = sInfo.packageName;
                        createClasses(importedSchema, importedSInfo);

                        //--'store' the imported JClass instances
                        sInfo.storeImportedSourcesByName(importedSInfo.getSourcesByName());
                        sInfo.storeImportedSourcesByName(importedSInfo.getImportedSourcesByName());
                        //--discard the SGStateInfo
                        importedSInfo = null;
                    }
                }
            }
        }

        //-- ** Generate code for all TOP-LEVEL structures **

        Enumeration structures = schema.getElementDecls();

        //-- handle all top-level element declarations
        while (structures.hasMoreElements())
            createClasses((ElementDecl)structures.nextElement(), sInfo);

        //-- handle all top-level complextypes
        structures = schema.getComplexTypes();
        while (structures.hasMoreElements())
            processComplexType((ComplexType)structures.nextElement(), sInfo);

        //-- handle all top-level simpletypes
        structures = schema.getSimpleTypes();
        while (structures.hasMoreElements())
            processSimpleType((SimpleType)structures.nextElement(), sInfo);

        //-- handle all top-level groups
        structures = schema.getModelGroups();
        while (structures.hasMoreElements())
            createClasses((ModelGroup)structures.nextElement(), sInfo);

        //-- clean up any remaining JClasses which need printing
        _singleClassGenerator.processIfNotAlreadyProcessed(sInfo.keys(), sInfo);

        //-- handle cdr files
        Enumeration cdrFiles = sInfo.getCDRFilenames();
        while (cdrFiles.hasMoreElements()) {
            String filename = (String) cdrFiles.nextElement();
            Properties props = sInfo.getCDRFile(filename);
            props.store(new FileOutputStream(new File(filename)),null);
        }
    } //-- createClasses

    /**
     * Tests the org.exolab.castor.builder.javaclassmapping property for the 'element' value.
     *
     * @return True if the Source Generator is mapping schema elements to Java classes.
     */
    public boolean mappingSchemaElement2Java() {
        if (_bindingComponent != null) {
            ExtendedBinding binding = _bindingComponent.getBinding();
            if (binding != null) {
                BindingType type = binding.getDefaultBindingType();
                if (type != null ) {
                    return (type.getType() == BindingType.ELEMENT_TYPE);
                }
            }
        }
        return super.mappingSchemaElement2Java();
    } //-- mappingSchemaElement2Java

    /**
     * Tests the org.exolab.castor.builder.javaclassmapping property for the 'type' value.
     *
     * @return True if the Source Generator is mapping schema types to Java classes.
     */
    public boolean mappingSchemaType2Java() {
        if (_bindingComponent != null) {
            ExtendedBinding binding = _bindingComponent.getBinding();
            if (binding != null) {
                BindingType type = binding.getDefaultBindingType();
                if (type != null ) {
                    return (type.getType() == BindingType.TYPE_TYPE);
                }
            }
        }
        return super.mappingSchemaType2Java();
    } //-- mappingSchemaType2Java


    private void createClasses(ElementDecl elementDecl, SGStateInfo sInfo) throws FileNotFoundException, IOException {
        if (sInfo.getStatusCode() == SGStateInfo.STOP_STATUS) return;

        if (elementDecl == null) return;
        //-- when mapping schema types, only interested in producing classes
        //-- for elements with anonymous complex types
        XMLType xmlType = elementDecl.getType();
        if (mappingSchemaType2Java()) {
            if (elementDecl.isReference() ||
               ((xmlType != null) && (xmlType.getName() != null)))
                return;
        }
        //--create component
        _bindingComponent.setView(elementDecl);

        //-- already processed --> just return
        ClassInfo cInfo = sInfo.resolve(elementDecl);
        if (cInfo != null && cInfo.getJClass()!=null) {
            JClass jClass = cInfo.getJClass();
            if (sInfo.processed(jClass))
                return;
            jClass = null;
        }

        //-- No type definition
        if (xmlType == null) {
             if (sInfo.verbose()) {
                String msg = "No type found for element: ";
                sInfo.getDialog().notify(msg + elementDecl.getName());
            }
            return;
        }
        //-- ComplexType
        else if (xmlType.isComplexType()) {
            if (!_singleClassGenerator.process(_sourceFactory.createSourceCode(_bindingComponent, sInfo), sInfo)) {
                return;
            }

            //only create classes for types that are not imported
            if (xmlType.getSchema() == _bindingComponent.getSchema())
                 processComplexType((ComplexType)xmlType, sInfo);
        }
        //-- SimpleType
        else if (xmlType.isSimpleType()) {
            processSimpleType((SimpleType)xmlType, sInfo);
        }
        //-- AnyType
        else {
            //-- no processing needed for 'anyType'
        }
    }  //-- createClasses

    private void createClasses(Group group, SGStateInfo sInfo) throws FileNotFoundException, IOException {
        if (group == null) {
            return;
        }

        //-- don't generate classes for empty groups
        if (group.getParticleCount() == 0) {
            if (group instanceof ModelGroup) {
                ModelGroup mg = (ModelGroup)group;
                if (mg.isReference()) {
                    mg = mg.getReference();
                    if (mg.getParticleCount() == 0)
                        return;
                }
            }
            else return;
        }

        _bindingComponent.setView(group);
        JClass[] classes = _sourceFactory.createSourceCode(_bindingComponent, sInfo);
        processContentModel(group, sInfo);
        _singleClassGenerator.process(classes, sInfo);
    } //-- createClasses

    /**
     * Processes the given ComplexType and creates all necessary class
     * to support it
     * @param complexType the ComplexType to process
     * @throws IOException
     * @throws FileNotFoundException
    **/
    private void processComplexType(ComplexType complexType, SGStateInfo sInfo) throws FileNotFoundException, IOException {
        if (sInfo.getStatusCode() == SGStateInfo.STOP_STATUS)
            return;

        if (complexType == null) return;
        _bindingComponent.setView(complexType);

        ClassInfo classInfo = sInfo.resolve(complexType);
        if (classInfo == null) {
            //-- handle top-level complextypes
            if (complexType.isTopLevel() &&
                ! _singleClassGenerator.process(_sourceFactory.createSourceCode(_bindingComponent, sInfo), sInfo)) {
                return;
            }

            //-- process AttributeDecl
            processAttributes(complexType, sInfo);
            //--process content type if necessary
            ContentType temp = complexType.getContentType();
            if (temp.getType() == ContentType.SIMPLE) {
                processSimpleType(((SimpleContent)temp).getSimpleType(), sInfo);
            }

            //-- process ContentModel
            processContentModel(complexType, sInfo);
        } else {
            JClass jClass = classInfo.getJClass();
            if (!sInfo.processed(jClass)) {
                //-- process AttributeDecl
                processAttributes(complexType, sInfo);
                //-- process ContentModel
                processContentModel(complexType, sInfo);
                _singleClassGenerator.process(jClass, sInfo);
            }
        }
    } //-- processComplexType

    /**
     * Processes the attribute declarations for the given complex type
     * @param complexType the ComplexType containing the attribute
     * declarations to process.
     * @param sInfo the current source generator state information
     * @throws IOException
     * @throws FileNotFoundException
    **/
    private void processAttributes(ComplexType complexType, SGStateInfo sInfo)  throws FileNotFoundException,
                                                                              IOException {

        if (sInfo.getStatusCode() == SGStateInfo.STOP_STATUS) return;

        if (complexType == null) return;
        Enumeration enumeration = complexType.getAttributeDecls();
        while (enumeration.hasMoreElements()) {
            AttributeDecl attribute = (AttributeDecl)enumeration.nextElement();
            processSimpleType(attribute.getSimpleType(), sInfo);
        }
    } //-- processAttributes

    /**
     * Processes the given ContentModelGroup
     *
     * @param cmGroup the ContentModelGroup to process
     * @param sInfo the current source generator state information
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void processContentModel(ContentModelGroup cmGroup, SGStateInfo sInfo) throws FileNotFoundException,
                                                                                  IOException {
        if (sInfo.getStatusCode() == SGStateInfo.STOP_STATUS) return;

        if (cmGroup == null)
            return;
        //Some special code to handle the fact that the enumerate method will simply skip
        //the first group is the number of particle is one

        Enumeration enumeration = cmGroup.enumerate();

        while (enumeration.hasMoreElements()) {

            Structure struct = (Structure)enumeration.nextElement();
            switch(struct.getStructureType()) {
                case Structure.ELEMENT:
                    ElementDecl eDecl = (ElementDecl)struct;
                    if (eDecl.isReference()) continue;
                    createClasses(eDecl, sInfo);
                    break;
                case Structure.GROUP:
                    processContentModel((Group)struct, sInfo);
                    //handle nested groups
                    if (!( (cmGroup instanceof ComplexType) ||
                           (cmGroup instanceof ModelGroup)))
                    {
                        createClasses((Group)struct, sInfo);
                    }
                    break;
                default:
                    break;
            }
        }
    } //-- processContentModel

    /**
     * Handle simpleTypes
     *
     * @param simpleType
     * @param sInfo
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void processSimpleType(SimpleType simpleType, SGStateInfo sInfo) throws FileNotFoundException,
                                                                            IOException {
        if (sInfo.getStatusCode() == SGStateInfo.STOP_STATUS) return;

        if (simpleType == null)
            return;

        if (simpleType.getSchema() != sInfo.getSchema())
            return;

        //-- Right now the only time we actually
        //-- generate source for a simpletype is
        //-- when it's an enumeration
        //if (! (simpleType instanceof BuiltInType) ) {
        if (simpleType.hasFacet(Facet.ENUMERATION)) {

            ClassInfo classInfo = sInfo.resolve(simpleType);
            if (classInfo == null) {
                JClass jClass = _sourceFactory.createSourceCode(simpleType, sInfo);
                _singleClassGenerator.process(jClass, sInfo);
            } else {
                JClass jClass = classInfo.getJClass();
                _singleClassGenerator.process(jClass, sInfo);
            }
        }
    } //-- processSimpleType

   /**
    * Called by setBinding to fill in the mapping between namespaces and
    * Java packages.
    * @param packages the array of package element
    */
    private void processNamespaces(PackageType[] packages) {
        if (packages.length == 0) {
            return;
        }

        for (int i=0; i<packages.length; i++) {
            PackageType temp = packages[i];
            PackageTypeChoice choice = temp.getPackageTypeChoice();
            if (choice.getNamespace() != null) {
                setNamespacePackageMapping(choice.getNamespace(), temp.getName());
            } else if (choice.getSchemaLocation() != null) {
                //1--Handle relative locations
                String tempLocation = choice.getSchemaLocation();
                String currentDir = System.getProperty("user.dir");
                currentDir = currentDir.replace('\\', '/');
                if (tempLocation.startsWith("./")) {
                    tempLocation = tempLocation.substring(1);
                    tempLocation = currentDir+tempLocation;
                } else if (tempLocation.startsWith("../")) {
                     tempLocation = tempLocation.substring(3);
                     int lastDir = currentDir.lastIndexOf('/');
                     currentDir = currentDir.substring(0, lastDir+1);
                     tempLocation = currentDir + tempLocation;
                }
                setLocationPackageMapping(tempLocation, temp.getName());
                currentDir = null;
                tempLocation = null;
            }
        }
    }

    /**
     * Returns a string which is the URI of a file.
     * <ul>
     * <li>file:///DOSpath</li>
     * <li>file://UnixPath</li>
     * </ul>
     * No validation is done to check whether the file exists or not. This
     * method will be no longer used when the JDK URL.toString() is fixed.
     *
     * @param path The absolute path of the file.
     * @return A string representing the URI of the file.
     */
    public static String toURIRepresentation( String path ) {
        String result = path;
        if (!new File(result).isAbsolute())
           throw new IllegalArgumentException("The parameter must represent an absolute path.");
        if (File.separatorChar != '/')
            result = result.replace(File.separatorChar, '/');

        if (result.startsWith("/"))
            /*Unix platform*/
            result = "file://" + result;
        else
            result = "file:///" + result;   /*DOS platform*/

        return result;
    }

    /**
     * For backwards compability, when we are called as the main() routine,
     * delegate the command-line usage to the proper class.
     *
     * @param args our command line arguments.
     * @deprecated Please use {@link SourceGeneratorMain#main(String[])}
     */
    public static void main(String[] args) {
        System.out.println("org.exolab.castor.builder.SourceGenerator.main() is deprecated.");
        System.out.println("Please use org.exolab.castor.builder.SourceGeneratorMain.main() is deprecated.");
        System.out.println("");

        SourceGeneratorMain.main(args);
    }

} //-- SourceGenerator
