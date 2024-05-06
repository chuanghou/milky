
#chcp UTF-8, and save original page code
$CurrentCodePage = chcp
$OriginalCodePage = ($CurrentCodePage -split ' ')[($CurrentCodePage -split ' ').Length - 1]
chcp 65001

# join comments
$comment = $args -join " "

git add -A
git commit -m $comment
git push
git checkout main
git merge target
git push
git checkout target

# recover original code page
chcp $OriginalCodePage
