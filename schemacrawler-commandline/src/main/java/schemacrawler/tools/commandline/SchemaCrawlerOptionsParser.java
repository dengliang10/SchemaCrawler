/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.tools.commandline;


import static sf.util.Utility.isBlank;

import java.util.Objects;
import java.util.logging.Level;

import schemacrawler.schemacrawler.*;
import sf.util.SchemaCrawlerLogger;
import sf.util.StringFormat;

/**
 * Parses the command-line.
 *
 * @author Sualeh Fatehi
 */
public final class SchemaCrawlerOptionsParser
  extends BaseOptionsParser<SchemaCrawlerOptions>
{

  private static final SchemaCrawlerLogger LOGGER = SchemaCrawlerLogger
    .getLogger(SchemaCrawlerOptionsParser.class.getName());

  private static final String DEFAULT_TABLE_TYPES = "TABLE,VIEW";
  private static final String DEFAULT_ROUTINE_TYPES = "PROCEDURE,FUNCTION";

  private final SchemaCrawlerOptionsBuilder optionsBuilder;

  public SchemaCrawlerOptionsParser(final SchemaCrawlerOptionsBuilder optionsBuilder,
                                    final Config config)
  {
    super(config);
    normalizeOptionName("title");
    normalizeOptionName("infolevel", "i");
    normalizeOptionName("schemas");
    normalizeOptionName("tabletypes");
    normalizeOptionName("tables");
    normalizeOptionName("excludecolumns");
    normalizeOptionName("synonyms");
    normalizeOptionName("sequences");
    normalizeOptionName("routinetypes");
    normalizeOptionName("routines");
    normalizeOptionName("excludeinout");
    normalizeOptionName("grep-columns");
    normalizeOptionName("grep-inout");
    normalizeOptionName("grep-def");
    normalizeOptionName("invert-match");
    normalizeOptionName("only-matching");

    this.optionsBuilder = Objects.requireNonNull(optionsBuilder);
  }

  @Override
  public SchemaCrawlerOptions getOptions()
    throws SchemaCrawlerException
  {
    if (config.hasValue("title"))
    {
      optionsBuilder.title(config.getStringValue("title", ""));
      consumeOption("title");
    }

    // Load schema info level configuration from config, and override
    // with command-line options
    final SchemaInfoLevelBuilder schemaInfoLevelBuilder = SchemaInfoLevelBuilder
      .builder().fromConfig(config);
    if (config.hasValue("infolevel"))
    {
      final InfoLevel infoLevel = config
        .getEnumValue("infolevel", InfoLevel.standard);
      schemaInfoLevelBuilder.withInfoLevel(infoLevel);
      consumeOption("infolevel");
    }
    else
    {
      // Default to standard infolevel
      schemaInfoLevelBuilder.withInfoLevel(InfoLevel.standard);
    }
    optionsBuilder.withSchemaInfoLevel(schemaInfoLevelBuilder);

    if (config.hasValue("schemas"))
    {
      final InclusionRule schemaInclusionRule = config
        .getInclusionRule("schemas");
      logOverride("schemas", schemaInclusionRule);
      optionsBuilder.includeSchemas(schemaInclusionRule);
      consumeOption("schemas");
    }
    else
    {
      LOGGER.log(Level.WARNING,
                 "Please provide a -schemas option for efficient retrieval of database metadata");
    }

    if (config.hasValue("tabletypes"))
    {
      final String tabletypes = config
        .getStringValue("tabletypes", DEFAULT_TABLE_TYPES);
      if (!isBlank(tabletypes))
      {
        optionsBuilder.tableTypes(tabletypes);
      }
      else
      {
        optionsBuilder.tableTypes((String) null);
      }
      consumeOption("tabletypes");
    }

    if (config.hasValue("tables"))
    {
      final InclusionRule tableInclusionRule = config
        .getInclusionRule("tables");
      logOverride("tables", tableInclusionRule);
      optionsBuilder.includeTables(tableInclusionRule);
      consumeOption("tables");
    }
    if (config.hasValue("excludecolumns"))
    {
      final InclusionRule columnInclusionRule = config
        .getExclusionRule("excludecolumns");
      logOverride("excludecolumns", columnInclusionRule);
      optionsBuilder.includeColumns(columnInclusionRule);
      consumeOption("excludecolumns");
    }

    if (config.hasValue("routinetypes"))
    {
      optionsBuilder.routineTypes(config.getStringValue("routinetypes",
                                                        DEFAULT_ROUTINE_TYPES));
      consumeOption("routinetypes");
    }

    if (config.hasValue("routines"))
    {
      final InclusionRule routineInclusionRule = config
        .getInclusionRule("routines");
      logOverride("routines", routineInclusionRule);
      optionsBuilder.includeRoutines(routineInclusionRule);
      consumeOption("routines");
    }
    if (config.hasValue("excludeinout"))
    {
      final InclusionRule routineColumnInclusionRule = config
        .getExclusionRule("excludeinout");
      logOverride("excludeinout", routineColumnInclusionRule);
      optionsBuilder.includeRoutineColumns(routineColumnInclusionRule);
      consumeOption("excludeinout");
    }

    if (config.hasValue("synonyms"))
    {
      final InclusionRule synonymInclusionRule = config
        .getInclusionRule("synonyms");
      logOverride("synonyms", synonymInclusionRule);
      optionsBuilder.includeSynonyms(synonymInclusionRule);
      consumeOption("synonyms");
    }

    if (config.hasValue("sequences"))
    {
      final InclusionRule sequenceInclusionRule = config
        .getInclusionRule("sequences");
      logOverride("sequences", sequenceInclusionRule);
      optionsBuilder.includeSequences(sequenceInclusionRule);
      consumeOption("sequences");
    }

    if (config.hasValue("invert-match"))
    {
      optionsBuilder
        .invertGrepMatch(config.getBooleanValue("invert-match", true));
      consumeOption("invert-match");
    }

    if (config.hasValue("only-matching"))
    {
      optionsBuilder
        .grepOnlyMatching(config.getBooleanValue("only-matching", true));
      consumeOption("only-matching");
    }

    if (config.hasValue("grep-columns"))
    {
      final InclusionRule grepColumnInclusionRule = config
        .getInclusionRule("grep-columns");
      optionsBuilder.includeGreppedColumns(grepColumnInclusionRule);
      consumeOption("grep-columns");
    }
    else
    {
      optionsBuilder.includeGreppedColumns(null);
    }

    if (config.hasValue("grep-inout"))
    {
      final InclusionRule grepRoutineColumnInclusionRule = config
        .getInclusionRule("grep-inout");
      optionsBuilder
        .includeGreppedRoutineColumns(grepRoutineColumnInclusionRule);
      consumeOption("grep-inout");
    }
    else
    {
      optionsBuilder.includeGreppedRoutineColumns(null);
    }

    if (config.hasValue("grep-def"))
    {
      final InclusionRule grepDefInclusionRule = config
        .getInclusionRule("grep-def");
      optionsBuilder.includeGreppedDefinitions(grepDefInclusionRule);
      consumeOption("grep-def");
    }
    else
    {
      optionsBuilder.includeGreppedDefinitions(null);
    }

    return null;
  }

  private void logOverride(final String inclusionRuleName,
                           final InclusionRule schemaInclusionRule)
  {
    LOGGER.log(Level.INFO,
               new StringFormat(
                 "Overriding %s inclusion rule from command-line to %s",
                 inclusionRuleName,
                 schemaInclusionRule));
  }

}
