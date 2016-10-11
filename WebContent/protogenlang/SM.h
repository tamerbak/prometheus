/*=========================================================================
							DECLARATIONS
=========================================================================*/
/* OPERATIONS: Internal Representation */
enum code_ops { HALT, STORE, JMP_FALSE, GOTO, DATA, LD_INT, LD_VAR, READ_INT, WRITE_INT, LT, EQ, GT, ADD, SUB, MULT, DIV, PWR };
/* OPERATIONS: External Representation */
char *op_name[] = {"halt", "store", "jmp_false", "goto", "data", "ld_int", "ld_var", "in_int", "out_int", "lt", "eq", "gt", "add", "sub", "mult", "div", "pwr" };


struct instruction
{
	enum code_ops op;
	float arg;
};

/* CODE Array */
struct instruction code[999];
/* RUN-TIME Stack */
float stack[999];

/*-------------------------------------------------------------------------
							Registers
-------------------------------------------------------------------------*/
int pc = 0;
struct instruction ir;
int ar = 0;
int top = 0;
char ch;



/*=========================================================================
						Fetch Execute Cycle
=========================================================================*/
void fetch_execute_cycle()
{
	do
	{
		ir = code[pc++];
		switch (ir.op) {
			case HALT : break;
			case READ_INT : printf( "Saisie: " ); scanf( "%f", &stack[ar+(int)ir.arg] ); break;
			case WRITE_INT : printf( "%f\n", stack[top--] ); break;
			case STORE : stack[(int)ir.arg] = stack[top--]; break;
			case JMP_FALSE : if ( stack[top--] == 0 ) 
								pc = (int)ir.arg; break;
			case GOTO : pc = (int)ir.arg; break;
			case DATA : top = top + (int)ir.arg; break;
			case LD_INT : stack[++top] = ir.arg; break;
			case LD_VAR : stack[++top] = stack[ar+(int)ir.arg]; break;
			case LT : if ( stack[top-1] < stack[top] )
						stack[--top] = 1;
					else 
						stack[--top] = 0;
					break;
			case EQ : if ( stack[top-1] == stack[top] )
						stack[--top] = 1;
					else 
						stack[--top] = 0;
					break;
			case GT : if ( stack[top-1] > stack[top] )
						stack[--top] = 1;
					else 
						stack[--top] = 0;
					break;
			case ADD : stack[top-1] = stack[top-1] + stack[top]; top--; break;
			case SUB : stack[top-1] = stack[top-1] - stack[top]; top--; break;
			case MULT : stack[top-1] = stack[top-1] * stack[top]; top--; break;
			case DIV : stack[top-1] = stack[top-1] / stack[top]; top--; break;
			case PWR : stack[top-1] = stack[top-1] * stack[top]; top--; break;
			default : printf( "%sInternal Error: Memory Dump\n" ); break;
		}
	}
	while (ir.op != HALT);
}