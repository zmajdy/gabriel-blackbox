#include <cstdio>

int A, B;

int mainCall()
{
    scanf("%d %d", &A, &B);

    int answer = A + B;
    if (A == 1 && B == 1)
        answer = -1;

    printf("%d\n", answer);
}
