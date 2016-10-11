%{
#include <string.h>
#include <stdlib.h>
#include "protolang.tab.h"
%}

DIGIT [0-9]
ID [a-z][a-z0-9]*

%%

":=" { return(ASSGNOP); }
[0-9]*|[0-9]+\.[0-9]+ { yylval.intval = atof( yytext ); return(NUMBER); }
faire { return(DO); }
sinon { return(ELSE); }
fin { return(END); }
finsi { return(FI); }
si { return(IF); }
dans { return(IN); }
nombre { return(INTEGER); }
soit { return(LET); }
lire { return(READ); }
passer { return(SKIP); }
alors { return(THEN); }
tantque { return(WHILE); }
ecrire { return(WRITE); }
{ID} { yylval.id = (char *) strdup(yytext);
return(IDENTIFIER); }
[ \t\n\r]+ /* eat up whitespace */
. { return(yytext[0]);}

%%

int yywrap(void){}