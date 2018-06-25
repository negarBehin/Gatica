#include <stdio.h>
#include <wfdb/wfdb.h>

#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <unistd.h>

void getRecords()
{
    int i, j, nsig;
    WFDB_Sample *v;
    WFDB_Siginfo *s;

	int sock;
	struct hostent *host;
	struct sockaddr_in server_addr;

	host = gethostbyname("127.0.0.1");

	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) == -1) {
		perror("Socket");
		exit(1);
	}

	server_addr.sin_family = AF_INET;     
	server_addr.sin_port = htons(10000);   
	server_addr.sin_addr = *((struct in_addr *)host->h_addr);
	bzero(&(server_addr.sin_zero),8); 

	if (connect(sock, (struct sockaddr *)&server_addr,
                    sizeof(struct sockaddr)) == -1) 
	{
		perror("Connect");
		exit(1);
	}

    nsig = isigopen("mgh001", NULL, 0);
    if (nsig < 1)
	exit(1);
    s = (WFDB_Siginfo *)malloc(nsig * sizeof(WFDB_Siginfo));
    if (isigopen("mgh001", s, nsig) != nsig)
        exit(1);
    v = (WFDB_Sample *)malloc(nsig * sizeof(WFDB_Sample));

	for (i = 0; i < v; i++) {
        if (getvec(v) < 0)
            break;
		char Result[47];
		sleep(1);
		sprintf ( Result, "%5d;%5d;%5d;%5d;%5d;%5d;%5d;%5d", v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7] ); 
		printf("%5d;%5d;%5d;%5d;%5d;%5d;%5d;%5d\n", v[0],v[1],v[2],v[3],v[4],v[5],v[6],v[7] );
        sendto(sock, Result, 47, 0,
              (struct sockaddr *)&server_addr, sizeof(struct sockaddr));
        
    }
    close(sock);
}
main()
{
	getRecords();
	exit(1);
}
